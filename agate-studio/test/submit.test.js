import test from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import fs from "node:fs/promises";
import os from "node:os";
import path from "node:path";
import { chromium } from "playwright";
import { installDom } from "../src/dom.js";
import { Runner } from "../src/runner.js";
test(
  "record input submit/button/image/reset; replay PIN → Weiter → delayed address page",
  { skip: process.env.AGATE_BROWSER_TEST !== "1", timeout: 30000 },
  async () => {
    const server = http.createServer((req, res) => {
      res.setHeader("content-type", "text/html");
      if (req.url.startsWith("/address")) {
        setTimeout(
          () =>
            res.end(
              '<select id="Ordinationsadresse"><option value="1">One</option><option value="2">Two</option></select>',
            ),
          350,
        );
      } else
        res.end(
          '<form action="/address"><input id="PIN" type="password"><input type="submit" value="Weiter" id="weiter"></form><input type="button" value="Action"><input type="reset" value="Reset"><input type="image" alt="Image" src="data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%2210%22 height=%2210%22%3E%3C/svg%3E">',
        );
    });
    await new Promise((r) => server.listen(0, "127.0.0.1", r));
    const url = "http://127.0.0.1:" + server.address().port;
    const root = await fs.mkdtemp(path.join(os.tmpdir(), "agate-submit-"));
    let browser;
    const runner = new Runner({ root });
    try {
      browser = await chromium.launch({
        headless: true,
        ...(process.env.AGATE_CHROMIUM
          ? {
              executablePath: process.env.AGATE_CHROMIUM,
              args: process.env.AGATE_CHROMIUM_ARGS.split(" "),
            }
          : {}),
      });
      const c = await browser.newContext();
      const events = [];
      await c.exposeBinding("agateEvent", (_, e) => events.push(e));
      await c.addInitScript(installDom);
      const p = await c.newPage();
      await p.goto(url);
      await p
        .getByRole("button", { name: "Action", exact: true })
        .evaluate((e) => e.click());
      for (const name of ["Action", "Reset", "Image"])
        await p.getByRole("button", { name, exact: true }).click();
      await p.locator("#PIN").fill("1234");
      await p.getByRole("button", { name: "Weiter", exact: true }).click();
      await p.locator("#Ordinationsadresse").waitFor();
      assert.ok(
        events.some(
          (e) =>
            e.kind === "diagnostic" && e.outcome === "ignored-synthetic-click",
        ),
      );
      assert.ok(
        !events.some((e) => e.op === "CLICK" && e.meta.isTrusted === false),
      );
      const clicks = events.filter((e) => e.op === "CLICK");
      for (const name of ["Action", "Reset", "Image", "Weiter"])
        assert.ok(
          clicks.some((e) => e.element.name === name),
          "Missing click " + name,
        );
      assert.equal(
        events.filter((e) => e.op === "FILL").at(-1).value,
        "{E[PASSWORD]}",
      );
      const weiter = clicks.find((e) => e.element.name === "Weiter");
      await runner.start(
        {
          timeout: 100,
          steps: [
            { type: "GUI", op: "OPEN", url, timeout: 2000 },
            {
              type: "GUI",
              op: "FILL",
              target: { css: "#PIN" },
              value: "{E[PASSWORD]}",
            },
            {
              type: "GUI",
              op: "CLICK",
              target: weiter.element.target,
              timeout: 2000,
            },
            {
              type: "GUI",
              op: "WAIT",
              target: { css: "#Ordinationsadresse" },
              state: "visible",
              timeout: 2000,
            },
            {
              type: "GUI",
              op: "SELECT",
              target: { css: "#Ordinationsadresse" },
              value: "2",
              timeout: 2000,
            },
          ],
        },
        { headed: false, vars: { E: { PASSWORD: "1234" } } },
      );
      await runner.run();
      assert.equal(runner.status, "completed", JSON.stringify(runner.state()));
      assert.equal(
        await runner.page.locator("#Ordinationsadresse").inputValue(),
        "2",
      );
    } finally {
      await browser?.close();
      await runner.close();
      await new Promise((r) => server.close(r));
      await fs.rm(root, { recursive: true, force: true });
    }
  },
);
