import test from "node:test";
import assert from "node:assert/strict";
import { parse, validate, resolveValue, expand } from "../src/model.js";
test("reject unsupported operations and missing page references", () => {
  assert.throws(
    () => parse("steps:\n  - type: GUI\n    op: MAGIC"),
    /unsupported/,
  );
  assert.throws(
    () =>
      validate({
        steps: [{ type: "GUI", op: "CLICK", target: "login.button" }],
      }),
    /Unknown element/,
  );
});
test("resolve all AGATE namespaces without silently swallowing missing buffers", () => {
  assert.equal(
    resolveValue("{R[Request.VSNR]}-{B[result]}-{E[password]}-{L[value]}", {
      R: { "Request.VSNR": "12" },
      B: { result: "ok" },
      E: { password: "secret" },
      L: { value: 1 },
    }),
    "12-ok-secret-1",
  );
  assert.throws(() => resolveValue("{B[missing]}", {}), /Unresolved/);
});
test("expand reusable and preserve local parameters", () => {
  const d = {
    steps: [
      {
        type: "GUI",
        op: "CALL",
        command: "login",
        parameters: { username: "a" },
      },
    ],
    reusable: {
      login: [
        {
          id: "fill",
          type: "GUI",
          op: "FILL",
          target: { testId: "username" },
          value: "{L[username]}",
        },
      ],
    },
  };
  validate(d);
  assert.equal(expand(d)[0].parameters.username, "a");
});
test("reject recursive reusable calls", () => {
  assert.throws(
    () =>
      validate({
        steps: [{ type: "GUI", op: "CALL", command: "loop" }],
        reusable: { loop: [{ type: "GUI", op: "CALL", command: "loop" }] },
      }),
    /Recursive/,
  );
});

test("YAML output orders id/type/op in steps and reusable without changing semantics", async () => {
  const { toYaml } = await import("../src/model.js");
  const doc = {
    steps: [{ op: "CALL", command: "block", type: "GUI", id: "run block" }],
    reusable: {
      block: [{ op: "WAIT", value: 10, type: "GUI", id: "wait for screen" }],
    },
  };
  const text = toYaml(doc);
  assert.ok(text.includes("- id: run block\n    type: GUI\n    op: CALL"));
  assert.ok(
    text.includes("- id: wait for screen\n      type: GUI\n      op: WAIT"),
  );
  assert.deepEqual(parse(text), doc);
});
