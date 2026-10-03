import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs/promises";
import os from "node:os";
import path from "node:path";
import { chromium } from "playwright";
const enabled = process.env.AGATE_BROWSER_TEST === "1";
test(
  "Studio: recorder → capture → modules → replay → debug → failure evidence",
  { skip: !enabled, timeout: 90000 },
  async () => {
    process.env.PORT = "4319";
    process.env.AGATE_DATA = await fs.mkdtemp(
      path.join(os.tmpdir(), "agate-studio-test-"),
    );
    const { getSession, closeServer } = await import("../src/server.js");
    let uiBrowser;
    const base = "http://127.0.0.1:4319";
    async function api(route, body = {}) {
      const r = await fetch(base + "/api/" + route, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(body),
      });
      const x = await r.json();
      assert.equal(r.status, 200, JSON.stringify(x));
      return x;
    }
    const settle = async () => {
      await new Promise((r) => setTimeout(r, 80));
      await getSession().browserQueue;
    };
    try {
      uiBrowser = await chromium.launch({
        headless: true,
        ...(process.env.AGATE_CHROMIUM
          ? {
              executablePath: process.env.AGATE_CHROMIUM,
              args: process.env.AGATE_CHROMIUM_ARGS.split(" "),
            }
          : {}),
      });
      const ui = await uiBrowser.newPage({
        viewport: { width: 1440, height: 1000 },
      });
      const errors = [];
      ui.on("pageerror", (e) => errors.push(e.message));
      await ui.goto(base);
      await ui.waitForSelector("#timeline");
      const initialVars = JSON.parse(await ui.locator("#vars").inputValue());
      assert.deepEqual(initialVars.R, {});
      assert.deepEqual(initialVars.B, {});
      assert.equal(initialVars.E.PASSWORD, "");
      assert.ok(!("L" in initialVars));
      await ui.locator('[data-tab="results"]').click();
      await ui
        .locator("#vars")
        .fill(JSON.stringify({ E: { PASSWORD: "demo" } }));
      await ui.locator('[data-tab="design"]').click();
      await api("record/start", { url: base + "/demo", headed: false });
      const p = getSession().page;
      const permissionState = (page) =>
        page.evaluate(
          async () =>
            (
              await navigator.permissions.query({
                name: "local-network-access",
              })
            ).state,
        );
      assert.equal(
        await permissionState(p),
        "granted",
        "Recorder local network permission",
      );
      await p.getByTestId("username").fill("milenko");
      await p.getByTestId("password").fill("demo");
      await p.getByTestId("login").click();
      await p.getByTestId("vsnr").fill("1234567890");
      await p.locator("#insurance").selectOption("oegk");
      await p.getByTestId("patient-search").click();
      await p.getByTestId("patient-status").waitFor();
      await settle();
      assert.ok(
        getSession().doc.steps.some(
          (s) => s.op === "FILL" && s.value === "{E[PASSWORD]}",
        ),
      );
      assert.ok(!JSON.stringify(getSession().doc).includes('"value":"demo"'));
      await api("record/mode", { mode: "assert" });
      await p.getByTestId("patient-status").click();
      await settle();
      assert.equal(getSession().doc.steps.at(-1).op, "CLICK");
      await api("step", {
        step: {
          op: "ASSERT",
          target: { testId: "patient-status" },
          assertion: { type: "TEXT_EQUALS", expected: "ACTIVE" },
        },
      });
      const capture = await api("capture");
      assert.ok(
        capture.elements.some(
          (e) => e.target.testId === "vsnr" && e.alternatives[0].matches === 1,
        ),
      );
      await api("capture/save", {
        name: "patient_search",
        elements: [{ key: "status", spec: { testId: "patient-status" } }],
        aria: true,
      });
      assert.ok(
        getSession().doc.pages.patient_search.aria.includes("Patient Search"),
      );
      await api("record/stop");
      assert.equal(
        getSession().doc.steps.filter((s) => s.op === "SELECT").length,
        1,
        "SELECT input/change should coalesce",
      );
      const diagnostics = await (
        await fetch(base + "/api/record/diagnostics")
      ).json();
      assert.ok(
        diagnostics.rows.some(
          (r) => r.op === "SELECT" && r.outcome === "added" && r.stepId,
        ),
      );
      assert.ok(
        diagnostics.rows.some(
          (r) => r.op === "SELECT" && r.outcome === "merged",
        ),
      );
      const rawLog = await (await fetch(base + "/api/record/log")).text();
      assert.ok(rawLog.includes('"sourceSequence"'));
      assert.ok(
        !rawLog.includes('"value":"demo"'),
        "Password must not appear in journal",
      );
      await ui.locator('[data-tab="diagnostics"]').click();
      await ui.locator("#refreshRecorderLog").click();
      await ui.waitForFunction(() =>
        document
          .querySelector("#recorderLog")
          .textContent.includes("sourceSequence"),
      );
      await ui.locator('[data-tab="design"]').click();
      await api("capture/auto");
      assert.equal(
        typeof getSession().doc.steps.find((s) => s.op === "FILL").target,
        "string",
      );
      await api("project/save", { name: "integration" });
      await api("project/load", { name: "integration" });
      await ui.screenshot({
        path:
          process.env.AGATE_SCREENSHOT ||
          path.join(process.env.AGATE_DATA, "studio.png"),
      });
      await api("run/start", {
        headed: false,
        vars: { E: { PASSWORD: "demo" } },
      });
      let r;
      for (let i = 0; i < 160; i++) {
        r = getSession().runner.state();
        if (["completed", "failed"].includes(r.status)) break;
        await new Promise((r) => setTimeout(r, 100));
      }
      assert.equal(r.status, "completed", JSON.stringify(r));
      assert.ok(r.results.length >= 7);
      assert.equal(
        await permissionState(getSession().runner.page),
        "granted",
        "Run local network permission",
      );
      await fs.access(
        path.join(process.env.AGATE_DATA, "runs", r.runId, "trace.zip"),
      );
      await api("run/start", {
        headed: false,
        debug: true,
        vars: { E: { PASSWORD: "demo" } },
      });
      assert.equal(getSession().runner.status, "paused");
      await api("run/next");
      assert.equal(getSession().runner.index, 1);
      await api("run/cancel");
      assert.equal(getSession().runner.status, "cancelled");
      const example = await fs.readFile(
        new URL("../examples/patient-search.yaml", import.meta.url),
        "utf8",
      );
      await api("document", { yaml: example.replaceAll("4310", "4319") });
      await api("run/start", {
        headed: false,
        vars: { R: { "Request.VSNR": "9876543210" }, E: { PASSWORD: "demo" } },
      });
      for (let i = 0; i < 100; i++) {
        r = getSession().runner.state();
        if (r.status === "completed" || r.status === "failed") break;
        await new Promise((r) => setTimeout(r, 100));
      }
      assert.equal(r.status, "completed", JSON.stringify(r));
      assert.equal(r.vars.B.patientStatus, "ACTIVE");
      assert.ok(r.results.some((s) => s.id === "enter_username"));
      await ui.locator('[data-tab="yaml"]').click();
      await ui.waitForFunction(() =>
        document.querySelector("#yamlText").value.includes("buffer_status"),
      );
      await ui.locator("#applyYaml").click();
      await ui.waitForFunction(() =>
        document.querySelector("#toast").textContent.includes("YAML valid"),
      );
      await ui.locator('[data-tab="modules"]').click();
      await ui.waitForSelector("#moduleList h3");
      await ui.locator('[data-tab="design"]').click();
      await ui.locator('.step[data-i="2"]').click();
      await ui.waitForSelector("#applyStep");
      assert.equal(await ui.locator("#edit_op").inputValue(), "FILL");
      await api("document", {
        doc: {
          steps: [
            { id: "open", type: "GUI", op: "OPEN", url: base + "/demo" },
            {
              id: "bad",
              type: "GUI",
              op: "CLICK",
              target: { role: "button", name: "Does not exist" },
            },
          ],
          timeout: 300,
        },
      });
      await api("run/start", { headed: false });
      for (let i = 0; i < 60; i++) {
        r = getSession().runner.state();
        if (r.status === "failed") break;
        await new Promise((r) => setTimeout(r, 100));
      }
      assert.equal(r.status, "failed");
      assert.ok(
        r.results.at(-1).explanation.candidates.some((c) => c.name === "Login"),
      );
      await ui.locator('[data-tab="results"]').click();
      await ui.locator('[data-result="1"]').click();
      await ui.waitForSelector("#repairOnce");
      assert.deepEqual(errors, []);
      const candidate = r.results
        .at(-1)
        .explanation.candidates.find((c) => c.name === "Login");
      await api("repair", {
        index: 1,
        spec: candidate.spec,
        persist: false,
        headed: false,
      });
      for (let i = 0; i < 60; i++) {
        r = getSession().runner.state();
        if (r.status === "completed" || r.status === "failed") break;
        await new Promise((r) => setTimeout(r, 100));
      }
      assert.equal(r.status, "completed");
      assert.equal(getSession().doc.steps[1].target.name, "Does not exist");
    } finally {
      await uiBrowser?.close();
      await closeServer();
      await fs.rm(process.env.AGATE_DATA, { recursive: true, force: true });
    }
  },
);
