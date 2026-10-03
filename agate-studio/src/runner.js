import { chromium } from "playwright";
import fs from "node:fs/promises";
import path from "node:path";
import {
  locator,
  targetSpec,
  resolveValue,
  expand,
  validate,
} from "./model.js";
export class Runner {
  constructor({ root = "data", emit = () => {} } = {}) {
    this.root = path.resolve(root);
    this.emit = emit;
    this.status = "idle";
    this.index = 0;
    this.results = [];
    this.logs = [];
    this.busy = false;
  }
  async start(doc, { headed = true, vars = {}, debug = false } = {}) {
    if (this.status === "running" || this.busy)
      throw Error("A run is already in progress");
    await this.close();
    validate(doc);
    this.doc = structuredClone(doc);
    this.steps = expand(this.doc);
    this.vars = structuredClone(vars);
    this.index = 0;
    this.results = [];
    this.logs = [];
    this.cancelled = false;
    this.pauseRequested = false;
    this.runId = new Date().toISOString().replace(/[:.]/g, "-");
    this.dir = path.join(this.root, "runs", this.runId);
    await fs.mkdir(this.dir, { recursive: true });
    try {
      this.browser = await chromium.launch({
        headless: !headed,
        ...(process.env.AGATE_CHROMIUM
          ? {
              executablePath: process.env.AGATE_CHROMIUM,
              args:
                process.env.AGATE_CHROMIUM_ARGS?.split(" ").filter(Boolean) ||
                [],
            }
          : {}),
      });
      this.context = await this.browser.newContext({
        acceptDownloads: true,
        permissions: ["local-network-access"],
      });
      this.context.setDefaultTimeout(doc.timeout || 5000);
      await this.context.tracing.start({
        screenshots: true,
        snapshots: true,
        sources: true,
      });
      this.page = await this.context.newPage();
      const hook = (p) => {
        p.on("console", (m) => this.log("console", m.type() + ": " + m.text()));
        p.on("pageerror", (e) => this.log("pageerror", e.message));
        p.on("response", (r) =>
          this.log(
            "network",
            `${r.status()} ${r.request().method()} ${r.url()}`,
          ),
        );
        p.on("dialog", (d) => d.dismiss());
      };
      hook(this.page);
      this.context.on("page", hook);
      this.status = debug ? "paused" : "ready";
      this.notify();
    } catch (e) {
      await this.close();
      this.status = "failed";
      throw e;
    }
    return this.state();
  }
  log(kind, text) {
    this.logs.push({ kind, text, time: Date.now() });
    if (this.logs.length > 1000) this.logs.shift();
  }
  state() {
    return {
      status: this.status,
      index: this.index,
      total: this.steps?.length || 0,
      results: this.results,
      logs: this.logs.slice(-150),
      runId: this.runId,
      vars: this.vars,
    };
  }
  notify() {
    this.emit(this.state());
  }
  async run() {
    if (this.busy) throw Error("Runner busy");
    this.pauseRequested = false;
    while (this.index < this.steps.length && !this.cancelled) {
      if (this.pauseRequested) {
        this.status = "paused";
        break;
      }
      if (this.steps[this.index].breakpoint) {
        this.steps[this.index].breakpoint = false;
        this.status = "paused";
        break;
      }
      if (!(await this.next())) break;
    }
    this.notify();
    return this.state();
  }
  async next() {
    if (this.busy) throw Error("Runner busy");
    if (
      !this.page ||
      ["completed", "failed", "cancelled"].includes(this.status)
    )
      throw Error("Start a new run");
    if (this.index >= this.steps.length) return false;
    this.busy = true;
    this.status = "running";
    const s = this.steps[this.index],
      n = this.index,
      start = Date.now();
    this.notify();
    const result = {
      index: n,
      id: s.id || `step_${n + 1}`,
      op: s.op,
      status: "running",
    };
    try {
      await this.context.tracing.group(result.id);
      await this.page
        .screenshot({ path: path.join(this.dir, `${n}-before.png`) })
        .catch(() => {});
      await fs
        .writeFile(
          path.join(this.dir, `${n}-before.aria.yaml`),
          await this.page.locator("body").ariaSnapshot(),
        )
        .catch(() => {});
      await this.execute(s);
      await fs
        .writeFile(
          path.join(this.dir, `${n}-after.aria.yaml`),
          await this.page.locator("body").ariaSnapshot(),
        )
        .catch(() => {});
      result.status = "passed";
      result.after = `${n}-after.png`;
      await this.page
        .screenshot({ path: path.join(this.dir, result.after) })
        .catch(() => {});
      result.before = `${n}-before.png`;
    } catch (e) {
      result.status = "failed";
      result.error = e.message;
      result.explanation = await this.explain(s).catch(() => null);
      result.before = `${n}-before.png`;
      result.after = `${n}-failed.png`;
      await this.page
        .screenshot({ path: path.join(this.dir, result.after) })
        .catch(() => {});
      this.status = this.cancelled ? "cancelled" : "failed";
    } finally {
      await this.context?.tracing.groupEnd().catch(() => {});
      result.duration = Date.now() - start;
      this.results.push(result);
      this.index++;
      this.busy = false;
      if (result.status === "passed")
        this.status = this.index === this.steps.length ? "completed" : "paused";
      if (this.cancelled) this.status = "cancelled";
      if (["completed", "failed", "cancelled"].includes(this.status))
        await this.finish();
      this.notify();
    }
    return result.status === "passed";
  }
  async execute(s) {
    const value = (x) =>
        resolveValue(x, {
          ...this.vars,
          L: { ...this.vars.L, ...s.parameters },
        }),
      spec = s.target ? targetSpec(s.target, this.doc) : null;
    const l = spec ? locator(this.page, spec) : null;
    // Each step may override the project timeout; restore the project default
    // for every subsequent step rather than leaking a previous override.
    this.context.setDefaultTimeout(s.timeout ?? this.doc.timeout ?? 30000);
    switch (s.op) {
      case "OPEN":
        await this.page.goto(value(s.url || s.value), {
          waitUntil: "domcontentloaded",
        });
        break;
      case "BACK":
        await this.page.goBack({ waitUntil: "domcontentloaded" });
        break;
      case "CLICK":
        await l.click();
        break;
      case "FILL":
        await l.fill(String(value(s.value)));
        break;
      case "SELECT":
        await l.selectOption(value(s.value));
        break;
      case "CHECK":
        await l.check();
        break;
      case "UNCHECK":
        await l.uncheck();
        break;
      case "HOVER":
        await l.hover();
        break;
      case "PRESS":
        await l.press(value(s.value));
        break;
      case "UPLOAD":
        await l.setInputFiles(value(s.value));
        break;
      case "DOWNLOAD": {
        const waiting = this.page.waitForEvent("download");
        await l.click();
        const d = await waiting;
        const file = path.basename(value(s.value || d.suggestedFilename()));
        await d.saveAs(path.join(this.dir, file));
        this.vars.B ||= {};
        this.vars.B[s.buffer || "download"] = path.join(this.dir, file);
        break;
      }
      case "WAIT":
        if (l)
          await l.waitFor({
            state: s.state || "visible",
            timeout: s.timeout ?? this.doc.timeout ?? 30000,
          });
        else await this.page.waitForTimeout(Number(value(s.value) || 1000));
        break;
      case "SCREENSHOT":
        await this.page.screenshot({
          path: path.join(this.dir, path.basename(s.value || "screenshot.png")),
          fullPage: true,
        });
        break;
      case "EXTRACT":
        this.vars.B ||= {};
        this.vars.B[s.buffer || "extracted"] = s.attribute
          ? await l.getAttribute(s.attribute)
          : s.source === "value"
            ? await l.inputValue()
            : await l.innerText();
        break;
      case "ASSERT_PAGE_STRUCTURE": {
        const page = this.doc.pages?.[s.target];
        if (!page?.aria) throw Error("Capture an ARIA baseline first");
        const actual = await this.page.locator("body").ariaSnapshot();
        if (actual.trim() !== value(page.aria).trim())
          throw Error(
            "ARIA structure differs from baseline (exact snapshot comparison)",
          );
        break;
      }
      case "ASSERT":
        await this.assert(
          l,
          s.assertion,
          value,
          s.timeout || this.doc.timeout || 5000,
        );
        break;
      default:
        throw Error("Unsupported operation " + s.op);
    }
  }
  async assert(l, a, value, timeout) {
    let last;
    const end = Date.now() + timeout;
    do {
      try {
        const expected = value(a.expected);
        let ok;
        switch (a.type) {
          case "VISIBLE":
            ok = await l.isVisible();
            break;
          case "HIDDEN":
            ok = !(await l.isVisible());
            break;
          case "ENABLED":
            ok = await l.isEnabled();
            break;
          case "DISABLED":
            ok = await l.isDisabled();
            break;
          case "TEXT_EQUALS":
            last = await l.innerText({ timeout: Math.min(timeout, 300) });
            ok = last.trim() === String(expected).trim();
            break;
          case "TEXT_CONTAINS":
            last = await l.innerText({ timeout: Math.min(timeout, 300) });
            ok = last.includes(String(expected));
            break;
          case "VALUE":
            last = await l.inputValue({ timeout: Math.min(timeout, 300) });
            ok = last === String(expected);
            break;
          case "COUNT":
            last = await l.count();
            ok = last === Number(expected);
            break;
          default:
            throw Error("Unknown assertion");
        }
        if (ok) return;
      } catch (e) {
        last = e.message;
      }
      await new Promise((r) => setTimeout(r, 100));
    } while (Date.now() < end);
    throw Error(
      `Assertion ${a.type} failed; expected ${value(a.expected)}, observed ${last}`,
    );
  }
  async explain(s) {
    const spec = targetSpec(s.target, this.doc);
    if (!spec) return null;
    const role =
      spec.role ||
      {
        SELECT: "combobox",
        FILL: "textbox",
        CLICK: "button",
        CHECK: "checkbox",
        UNCHECK: "checkbox",
      }[s.op];
    if (!role)
      return {
        reason:
          "Target was not resolved; check the previous action and the current page.",
        configured: spec,
        candidates: [],
      };
    const candidates = await this.page.getByRole(role).evaluateAll((els) =>
      els.slice(0, 30).map((e) => ({
        name:
          e.getAttribute("aria-label") ||
          (e.labels &&
            [...e.labels].map((l) => l.textContent.trim()).join(" ")) ||
          e.getAttribute("alt") ||
          (["submit", "button", "reset"].includes(e.type)
            ? e.value || e.textContent?.trim()
            : e.textContent?.trim()),
        testId: e.getAttribute("data-testid"),
      })),
    );
    for (const c of candidates) {
      c.spec = c.testId
        ? { testId: c.testId }
        : { role, name: c.name, exact: true };
      c.matches = await locator(this.page, c.spec)
        .count()
        .catch(() => 0);
    }
    return {
      reason:
        "Check target uniqueness, page state, frame context, or changed label. Candidates are suggestions only; review before rerunning.",
      configured: spec,
      candidates,
    };
  }
  pause() {
    this.pauseRequested = true;
    return this.state();
  }
  async cancel() {
    this.cancelled = true;
    await this.browser?.close();
    if (!this.busy) {
      this.status = "cancelled";
      await this.finish();
      this.notify();
    }
    return this.state();
  }
  async finish() {
    if (!this.context) return;
    await this.context.tracing
      .stop({ path: path.join(this.dir, "trace.zip") })
      .catch(() => {});
    await fs.writeFile(
      path.join(this.dir, "report.json"),
      JSON.stringify(this.state(), null, 2),
    );
    await fs.writeFile(
      path.join(this.dir, "test.json"),
      JSON.stringify(this.doc, null, 2),
    );
  }
  async close() {
    await this.browser?.close().catch(() => {});
    this.browser = null;
    this.context = null;
    this.page = null;
  }
}
