import express from "express";
import { chromium } from "playwright";
import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { installDom } from "./dom.js";
import {
  YAML,
  parse,
  locator,
  validate,
  toYaml,
  orderedStep,
} from "./model.js";
import { RecorderJournal } from "./recorder-journal.js";
import { Runner } from "./runner.js";
const base = path.resolve(path.dirname(fileURLToPath(import.meta.url)), ".."),
  root = path.resolve(process.env.AGATE_DATA || path.join(base, "data"));
const app = express(),
  port = Number(process.env.PORT || 4310),
  clients = new Set();
let browser,
  context,
  page,
  recording = false,
  mode = "record",
  picked = null,
  browserQueue = Promise.resolve();
let doc = {
  name: "Patient search",
  version: 1,
  timeout: 30000,
  pages: {},
  reusable: {},
  steps: [],
};
function publish() {
  const payload = JSON.stringify({
    doc,
    yaml: toYaml(doc),
    recording,
    mode,
    picked,
    url: page?.url(),
    run: runner.state(),
  });
  for (const c of clients) c.write("data: " + payload + "\n\n");
}
const journal = new RecorderJournal(root);
const runner = new Runner({ root, emit: publish });
app.use((req, res, next) => {
  const origin = req.headers.origin;
  if (
    origin &&
    origin !== `http://127.0.0.1:${port}` &&
    origin !== `http://localhost:${port}`
  )
    return res
      .status(403)
      .json({ error: "Only same-origin local requests allowed" });
  if (
    !["127.0.0.1", "localhost"].includes((req.headers.host || "").split(":")[0])
  )
    return res.status(403).end();
  next();
});
app.use(express.json({ limit: "5mb" }));
app.use(express.static(path.join(base, "public")));
app.use("/runs", express.static(path.join(root, "runs")));
const api = (route, fn) =>
  app.post("/api/" + route, async (req, res) => {
    try {
      res.json(await fn(req.body));
    } catch (e) {
      res.status(400).json({ error: e.message });
    }
  });
app.get("/api/events", (req, res) => {
  res.set({
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache",
    Connection: "keep-alive",
  });
  res.flushHeaders();
  clients.add(res);
  publish();
  req.on("close", () => clients.delete(res));
});
app.get("/api/state", (req, res) =>
  res.json({
    doc,
    yaml: toYaml(doc),
    recording,
    mode,
    picked,
    url: page?.url(),
    run: runner.state(),
  }),
);
const safe = (n) => {
  if (!/^[a-zA-Z0-9_-]{1,80}$/.test(n))
    throw Error("Name: letters, digits, underscore, dash only");
  return n;
};
const id = (s) =>
  (s || "element")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_|_$/g, "")
    .slice(0, 50) || "element";
async function frameSpecs(frame) {
  const out = [];
  while (frame.parentFrame()) {
    const e = await frame.frameElement();
    const css = await e.evaluate((el) =>
      el.id
        ? "#" + CSS.escape(el.id)
        : el.name
          ? `iframe[name="${CSS.escape(el.name)}"]`
          : `iframe:nth-of-type(${[...el.parentElement.children].filter((x) => x.tagName === "IFRAME").indexOf(el) + 1})`,
    );
    out.unshift(css);
    frame = frame.parentFrame();
  }
  return out;
}
async function enrich(element, frame) {
  const frames = await frameSpecs(frame);
  for (const a of element.alternatives) {
    if (frames.length) a.spec.frames = frames;
    try {
      a.matches = await locator(page, a.spec).count();
    } catch {
      a.matches = 0;
    }
  }
  const chosen =
    element.alternatives.find((a) => a.matches === 1) ||
    element.alternatives[0];
  element.target = chosen.spec;
  return element;
}
function addStep(s) {
  const last = doc.steps.at(-1);
  if (
    (s.op === "FILL" || (s.op === "SELECT" && last?.value === s.value)) &&
    last?.op === s.op &&
    JSON.stringify(last.target) === JSON.stringify(s.target)
  ) {
    last.value = s.value;
    publish();
    return { stepId: last.id, outcome: "merged" };
  }
  s.id ||= `${s.op.toLowerCase()}_${Date.now()}_${doc.steps.length}`;
  s.type = "GUI";
  doc.steps.push(orderedStep(s));
  publish();
  return { stepId: s.id, outcome: "added" };
}
async function event(source, data) {
  const evidence = {
    kind: data.kind,
    op: data.op,
    meta: data.meta,
    frameUrl: source.frame.url(),
  };
  // Record source evidence before asynchronous locator checks or navigation.
  await journal.append({ ...evidence, outcome: data.outcome || "received" });
  if (data.kind === "diagnostic") return;
  if (!recording) {
    await journal.append({ ...evidence, outcome: "ignored-not-recording" });
    return;
  }
  const original = structuredClone(data.element);
  try {
    data.element = await enrich(data.element, source.frame);
  } catch (e) {
    data.element = original;
    await journal.append({
      ...evidence,
      outcome: "locator-check-failed",
      error: e.message,
    });
  }
  if (data.kind === "pick" || data.kind === "assert") {
    picked = data;
    publish();
    return;
  }
  if (mode !== "record") {
    await journal.append({ ...evidence, outcome: "ignored-mode", mode });
    return;
  }
  const result = addStep({
    op: data.op,
    page: data.element.pageName,
    target: data.element.target,
    ...(data.value !== undefined ? { value: data.value } : {}),
  });
  await journal.append({
    ...evidence,
    ...result,
    target: data.element.target,
    alternatives: data.element.alternatives,
  });
}
async function startBrowser(url, headed = true) {
  if (!/^https?:\/\//.test(url)) throw Error("Use http:// or https:// URL");
  await browser?.close();
  await browserQueue;
  await journal.start();
  browser = await chromium.launch({
    headless: !headed,
    ...(process.env.AGATE_CHROMIUM
      ? {
          executablePath: process.env.AGATE_CHROMIUM,
          args:
            process.env.AGATE_CHROMIUM_ARGS?.split(" ").filter(Boolean) || [],
        }
      : {}),
  });
  context = await browser.newContext({
    acceptDownloads: true,
    permissions: ["local-network-access"],
  });
  await context.exposeBinding("agateEvent", (source, data) => {
    browserQueue = browserQueue
      .then(() => event(source, data))
      .catch(async (e) => {
        console.error("Recorder:", e.message);
        await journal.append({
          kind: "error",
          outcome: "recorder-error",
          error: e.message,
        });
      });
    return browserQueue;
  });
  await context.addInitScript(installDom);
  context.on("page", (p) => {
    page = p;
    p.on("dialog", (d) => d.dismiss());
    p.on("framenavigated", (f) => {
      browserQueue = browserQueue
        .then(() =>
          journal.append({
            kind: "navigation",
            url: f.url(),
            mainFrame: f === p.mainFrame(),
          }),
        )
        .catch(console.error);
    });
    p.on("domcontentloaded", () => setMode(mode).catch(() => {}));
  });
  page = await context.newPage();
  recording = true;
  await page.goto(url, { waitUntil: "domcontentloaded" });
  await setMode("record");
  addStep({ op: "OPEN", url });
  return { ok: true };
}
async function setMode(m) {
  mode = m;
  for (const p of context?.pages() || [])
    for (const f of p.frames())
      await f
        .evaluate((m) => window.__agate?.setMode(m), recording ? m : "off")
        .catch(() => {});
  publish();
}
app.get("/api/record/diagnostics", async (req, res) => {
  await browserQueue;
  res.json(journal.state());
});
app.get("/api/record/log", async (req, res) => {
  await browserQueue;
  if (!journal.file)
    return res.status(404).json({ error: "Start recording first" });
  res.download(journal.file, "agate-recorder-" + journal.id + ".jsonl");
});
api("record/start", (b) => startBrowser(b.url, b.headed !== false));
api("record/stop", async () => {
  await page?.evaluate(() => document.activeElement?.blur()).catch(() => {});
  await browserQueue;
  recording = false;
  await setMode("off");
  return { ok: true };
});
api("record/mode", async (b) => {
  if (!["record", "pick", "assert", "off"].includes(b.mode))
    throw Error("Invalid mode");
  await setMode(b.mode);
  return { ok: true };
});
api("record/navigate", async (b) => {
  if (!page) throw Error("Start browser first");
  await page.goto(b.url, { waitUntil: "domcontentloaded" });
  if (recording) addStep({ op: "OPEN", url: b.url });
  return { ok: true };
});
api("capture", async () => {
  if (!page) throw Error("Open target browser first");
  const elements = [];
  for (const f of page.frames()) {
    const items = await f
      .evaluate(() => window.__agate?.all() || [])
      .catch(() => []);
    for (const e of items) elements.push(await enrich(e, f));
  }
  return { elements, url: page.url() };
});
api("highlight", async (b) => {
  if (!page) throw Error("No browser");
  const l = locator(page, b.spec);
  await l.evaluate((e) => {
    e.style.outline = "3px solid #44d7b6";
    setTimeout(() => (e.style.outline = ""), 1800);
  });
  return { ok: true };
});
api("capture/save", async (b) => {
  const name = safe(b.name);
  doc.pages ||= {};
  const elements = {};
  for (const e of b.elements) {
    const k = safe(e.key);
    elements[k] = e.spec;
  }
  doc.pages[name] = { url: page?.url(), elements };
  if (b.aria) doc.pages[name].aria = await page.locator("body").ariaSnapshot();
  publish();
  return { ok: true };
});
api("capture/auto", async () => {
  doc.pages ||= {};
  let count = 0;
  const groups = new Map();
  for (const s of doc.steps) {
    if (s.op === "OPEN") {
      const url = new URL(s.url);
      const name = id(url.pathname === "/" ? "home" : url.pathname);
      groups.set(s, name);
      count++;
    }
  }
  let current = "home";
  for (const s of doc.steps) {
    if (groups.has(s)) {
      current = groups.get(s);
      doc.pages[current] ||= { url: s.url, elements: {} };
    } else if (s.target && typeof s.target === "object") {
      if (s.page) current = id(s.page);
      doc.pages[current] ||= { elements: {} };
      const key = id(
        s.target.name || s.target.testId || s.target.label || s.target.css,
      );
      let k = key,
        i = 2;
      while (
        doc.pages[current].elements[k] &&
        JSON.stringify(doc.pages[current].elements[k]) !==
          JSON.stringify(s.target)
      )
        k = key + "_" + i++;
      doc.pages[current].elements[k] = s.target;
      s.target = current + "." + k;
    }
  }
  publish();
  return {
    ok: true,
    count,
    note: "Page grouping uses visible headings and OPEN steps; review names for complex SPAs.",
  };
});
api("document", async (b) => {
  const d = b.yaml ? parse(b.yaml) : validate(b.doc);
  doc = d;
  publish();
  return { ok: true };
});
api("step", async (b) => {
  addStep(b.step);
  return { ok: true };
});
api("project/save", async (b) => {
  validate(doc);
  const folder = path.join(root, "projects", safe(b.name));
  await fs.mkdir(folder, { recursive: true });
  await fs.writeFile(path.join(folder, "project.yaml"), toYaml(doc));
  await fs.mkdir(path.join(folder, "gui", "pages"), { recursive: true });
  for (const [name, p] of Object.entries(doc.pages || {}))
    await fs.writeFile(
      path.join(folder, "gui", "pages", safe(name) + ".yaml"),
      YAML.stringify({ page: name, ...p }),
    );
  return { ok: true, path: folder };
});
api("project/load", async (b) => {
  doc = parse(
    await fs.readFile(
      path.join(root, "projects", safe(b.name), "project.yaml"),
      "utf8",
    ),
  );
  publish();
  return { ok: true };
});
app.get("/api/projects", async (req, res) => {
  await fs.mkdir(path.join(root, "projects"), { recursive: true });
  res.json(await fs.readdir(path.join(root, "projects")));
});
api("run/start", async (b) => {
  if (recording) {
    await page?.evaluate(() => document.activeElement?.blur()).catch(() => {});
    await browserQueue;
    recording = false;
    await setMode("off");
  }
  await runner.start(doc, b);
  if (!b.debug) runner.run().catch(console.error);
  return runner.state();
});
api("run/next", () => runner.next());
api("run/resume", () => {
  runner.run().catch(console.error);
  return runner.state();
});
api("run/pause", () => runner.pause());
api("run/cancel", () => runner.cancel());
api("repair", async (b) => {
  if (!runner.doc) throw Error("No failed run");
  const repaired = structuredClone(runner.doc);
  const step = runner.steps[b.index];
  if (!step) throw Error("Unknown step");
  let target = step.target;
  function replace(steps) {
    for (const s of steps) {
      if (s.id === step.id) s.target = b.spec;
    }
  }
  if (b.persist && typeof target === "string") {
    const [p, ...key] = target.split(".");
    doc.pages[p].elements[key.join(".")] = b.spec;
    repaired.pages[p].elements[key.join(".")] = b.spec;
  } else {
    replace(repaired.steps);
    for (const steps of Object.values(repaired.reusable || {})) replace(steps);
    if (b.persist) {
      replace(doc.steps);
      for (const steps of Object.values(doc.reusable || {})) replace(steps);
    }
  }
  validate(repaired);
  await runner.start(repaired, {
    headed: b.headed !== false,
    vars: b.vars || {},
  });
  runner.run().catch(console.error);
  publish();
  return { ok: true, note: "Rerunning from beginning with reviewed locator" };
});
api("health", async () => {
  if (!page) throw Error("Open target browser first");
  const out = [];
  for (const [p, module] of Object.entries(doc.pages || {}))
    for (const [e, spec] of Object.entries(module.elements || {})) {
      let matches = 0;
      try {
        matches = await locator(page, spec).count();
      } catch {}
      out.push({
        target: p + "." + e,
        matches,
        status:
          matches === 1 ? "unique" : matches === 0 ? "missing" : "ambiguous",
        note: "Checks current page only; other pages may be missing normally",
      });
    }
  return out;
});
api("trace/open", async (b) => {
  if (!runner.runId) throw Error("No run");
  await fs.access(path.join(runner.dir, "trace.zip"));
  const { spawn } = await import("node:child_process");
  const child = spawn(
    process.execPath,
    [
      path.join(base, "node_modules/playwright/cli.js"),
      "show-trace",
      path.join(runner.dir, "trace.zip"),
    ],
    { stdio: "ignore" },
  );
  child.on("error", console.error);
  return { ok: true };
});
app.get("/demo", (req, res) =>
  res.sendFile(path.join(base, "public", "demo.html")),
);
const server = app.listen(port, "127.0.0.1", () =>
  console.log(`AGATE Studio: http://127.0.0.1:${port}`),
);
async function shutdown() {
  await browser?.close();
  await runner.close();
  server.close();
  process.exit(0);
}
process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
// Import-only hooks for integration tests; never exposed through HTTP.
export function getSession() {
  return { page, context, doc, runner, browserQueue, journal };
}
export async function closeServer() {
  await browser?.close();
  await runner.close();
  for (const c of clients) c.end();
  await new Promise((resolve) => server.close(resolve));
}
