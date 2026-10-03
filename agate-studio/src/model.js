import YAML from "yaml";
export const OPS = [
  "OPEN",
  "BACK",
  "CLICK",
  "FILL",
  "SELECT",
  "CHECK",
  "UNCHECK",
  "HOVER",
  "PRESS",
  "UPLOAD",
  "DOWNLOAD",
  "WAIT",
  "ASSERT",
  "EXTRACT",
  "SCREENSHOT",
  "ASSERT_PAGE_STRUCTURE",
  "CALL",
];
export function parse(text) {
  const doc = YAML.parse(text);
  validate(doc);
  return doc;
}
export function validate(doc) {
  if (!doc || !Array.isArray(doc.steps)) throw Error("steps must be an array");
  const ids = new Set();
  const check = (steps, stack = []) =>
    steps.forEach((s, i) => {
      if (!s || s.type !== "GUI" || !OPS.includes(s.op))
        throw Error(`Step ${i + 1}: unsupported GUI operation`);
      if (s.id && !stack.length && ids.has(s.id))
        throw Error(`Duplicate step id: ${s.id}`);
      if (s.id && !stack.length) ids.add(s.id);
      if (s.op === "CALL") {
        const name = s.command;
        if (!doc.reusable?.[name]) throw Error(`Unknown reusable: ${name}`);
        if (stack.includes(name)) throw Error("Recursive reusable");
        check(doc.reusable[name], [...stack, name]);
      }
      if (s.op === "OPEN" && typeof (s.url || s.value) !== "string")
        throw Error("OPEN requires URL");
      if (
        ["FILL", "SELECT", "PRESS", "UPLOAD"].includes(s.op) &&
        s.value === undefined
      )
        throw Error(`${s.op} requires value`);
      if (
        !["OPEN", "BACK", "WAIT", "SCREENSHOT", "CALL"].includes(s.op) &&
        !s.target
      )
        throw Error(`Step ${s.id || i}: target required`);
      if (
        typeof s.target === "string" &&
        s.op !== "ASSERT_PAGE_STRUCTURE" &&
        !doc.pages?.[s.target.split(".")[0]]?.elements?.[
          s.target.split(".").slice(1).join(".")
        ]
      )
        throw Error(`Unknown element: ${s.target}`);
      if (
        s.op === "ASSERT" &&
        ![
          "VISIBLE",
          "HIDDEN",
          "ENABLED",
          "DISABLED",
          "TEXT_EQUALS",
          "TEXT_CONTAINS",
          "VALUE",
          "COUNT",
        ].includes(s.assertion?.type)
      )
        throw Error("Unknown assertion type");
    });
  check(doc.steps);
  return doc;
}
export function resolveValue(value, vars) {
  if (typeof value !== "string") return value;
  return value.replace(/\{([RBEL])\[([^\]]+)\]\}/g, (_, scope, key) => {
    const v = vars?.[scope]?.[key];
    if (v === undefined) throw Error(`Unresolved buffer: {${scope}[${key}]}`);
    return String(v);
  });
}
export function targetSpec(target, doc) {
  if (typeof target === "object") return target;
  const [page, ...name] = target.split(".");
  return doc.pages?.[page]?.elements?.[name.join(".")];
}
export function locator(page, spec) {
  if (!spec) throw Error("Missing locator");
  let root = page;
  for (const frame of spec.frames || []) root = root.frameLocator(frame);
  if (spec.scope) root = locator(root, spec.scope);
  let l;
  if (spec.testId) l = root.getByTestId(spec.testId);
  else if (spec.role)
    l = root.getByRole(spec.role, {
      ...(spec.name !== undefined
        ? { name: spec.name, exact: spec.exact !== false }
        : {}),
    });
  else if (spec.label)
    l = root.getByLabel(spec.label, { exact: spec.exact !== false });
  else if (spec.text !== undefined)
    l = root.getByText(spec.text, { exact: spec.exact !== false });
  else if (spec.placeholder)
    l = root.getByPlaceholder(spec.placeholder, { exact: true });
  else if (spec.css) l = root.locator(spec.css);
  else
    throw Error(
      "Locator requires testId, role, label, text, placeholder or css",
    );
  if (spec.nth !== undefined) l = l.nth(spec.nth);
  return l;
}
export function expand(doc) {
  const out = [];
  function add(steps, parameters = {}) {
    for (const s of steps) {
      if (s.op === "CALL")
        add(doc.reusable[s.command], { ...parameters, ...s.parameters });
      else out.push({ ...s, parameters });
    }
  }
  add(doc.steps);
  return out;
}
export { YAML };

export function orderedStep(step) {
  const out = {};
  for (const key of [
    "id",
    "type",
    "op",
    "page",
    "url",
    "target",
    "value",
    "assertion",
    "timeout",
    "state",
    "buffer",
    "source",
    "attribute",
    "command",
    "parameters",
    "breakpoint",
  ])
    if (Object.hasOwn(step, key)) out[key] = step[key];
  for (const [key, value] of Object.entries(step))
    if (!Object.hasOwn(out, key)) out[key] = value;
  return out;
}
export function toYaml(doc) {
  const ordered = { ...doc, steps: doc.steps.map(orderedStep) };
  if (doc.reusable)
    ordered.reusable = Object.fromEntries(
      Object.entries(doc.reusable).map(([name, steps]) => [
        name,
        steps.map(orderedStep),
      ]),
    );
  return YAML.stringify(ordered);
}
