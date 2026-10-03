const $ = (id) => document.getElementById(id),
  esc = (s) =>
    String(s ?? "").replace(
      /[&<>"']/g,
      (c) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[c],
    );
let state = { doc: { steps: [], pages: {}, reusable: {} }, run: {} },
  selected = -1,
  scan = [],
  checked = new Set(),
  yamlDirty = false;
function toast(text, error = false) {
  $("toast").textContent = text;
  $("toast").className = error ? "error" : "";
  $("toast").style.display = "block";
}
async function api(route, body = {}) {
  const res = await fetch("/api/" + route, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const result = await res.json();
  if (!res.ok) throw Error(result.error);
  return result;
}
function handler(id, fn) {
  $(id).onclick = async () => {
    const b = $(id);
    b.disabled = true;
    try {
      await fn();
    } catch (e) {
      toast(e.message, true);
    } finally {
      b.disabled = false;
    }
  };
}
function tab(name) {
  document.querySelectorAll(".view").forEach((v) => (v.hidden = v.id !== name));
  document
    .querySelectorAll("nav button")
    .forEach((b) => b.classList.toggle("active", b.dataset.tab === name));
  $("title").textContent = {
    design: "Design your next test.",
    capture: "Capture what matters.",
    modules: "A home for every locator.",
    yaml: "Readable. Editable. Yours.",
    results: "See every step.",
    guide: "Explore AGATE Studio.",
    diagnostics: "See what the recorder received.",
  }[name];
}
document
  .querySelectorAll("nav button")
  .forEach((b) => (b.onclick = () => tab(b.dataset.tab)));
$("url").value = location.origin + "/demo";
async function update() {
  try {
    await api("document", { doc: state.doc });
  } catch (e) {
    state = await (await fetch("/api/state")).json();
    render();
    throw e;
  }
}
function specText(t) {
  return typeof t === "string" ? t : JSON.stringify(t || {});
}
function render() {
  const d = state.doc;
  $("count").textContent = d.steps.length;
  $("record").textContent = state.recording ? "● Recording" : "● Record";
  $("timeline").innerHTML =
    d.steps
      .map(
        (s, i) =>
          `<div class="step ${selected === i ? "selected" : ""}" data-i="${i}"><input type="checkbox" data-select="${i}" ${checked.has(i) ? "checked" : ""}><span class="num">${i + 1}</span><span class="op">${esc(s.op)}</span><div class="details"><strong>${esc(s.id)}</strong><small>${esc(s.op === "OPEN" ? s.url : specText(s.target))}</small></div>${s.breakpoint ? '<span class="failed">●</span>' : ""}<button class="icon" data-up="${i}">↑</button><button class="icon" data-down="${i}">↓</button><button class="icon" data-delete="${i}">×</button></div>`,
      )
      .join("") || '<p class="hint">Start recording or add a manual step.</p>';
  $("timeline")
    .querySelectorAll(".step")
    .forEach(
      (e) =>
        (e.onclick = (ev) => {
          if (ev.target.closest("button,input")) return;
          selected = Number(e.dataset.i);
          render();
          inspector();
        }),
    );
  $("timeline")
    .querySelectorAll("[data-select]")
    .forEach(
      (e) =>
        (e.onchange = () =>
          e.checked
            ? checked.add(+e.dataset.select)
            : checked.delete(+e.dataset.select)),
    );
  for (const name of ["up", "down", "delete"])
    $("timeline")
      .querySelectorAll("[data-" + name + "]")
      .forEach(
        (e) =>
          (e.onclick = async () => {
            try {
              const i = +e.dataset[name];
              if (name === "delete") d.steps.splice(i, 1);
              else {
                const j = i + (name === "up" ? -1 : 1);
                if (j >= 0 && j < d.steps.length)
                  [d.steps[i], d.steps[j]] = [d.steps[j], d.steps[i]];
              }
              selected = -1;
              checked.clear();
              await update();
              inspector();
            } catch (e) {
              toast(e.message, true);
            }
          }),
      );
  if (!yamlDirty) $("yamlText").value = state.yaml || "";
  $("moduleList").innerHTML =
    Object.entries(d.pages || {})
      .map(
        ([name, p]) =>
          `<h3>${esc(name)}</h3><pre>${esc(JSON.stringify(p, null, 2))}</pre><button data-structure="${esc(name)}">Add structure assertion</button>`,
      )
      .join("") ||
    '<p class="hint">Capture a page or create modules from the timeline.</p>';
  $("moduleList")
    .querySelectorAll("[data-structure]")
    .forEach(
      (b) =>
        (b.onclick = async () => {
          try {
            await api("step", {
              step: {
                op: "ASSERT_PAGE_STRUCTURE",
                target: b.dataset.structure,
              },
            });
            toast(
              "Structure assertion added. Capture its ARIA baseline before running.",
            );
          } catch (e) {
            toast(e.message, true);
          }
        }),
    );
  $("reusableList").textContent = JSON.stringify(d.reusable || {}, null, 2);
  renderPicked();
  renderRun();
}
function field(name, value, textarea = false) {
  return `<div class="field"><label>${esc(name)}</label>${textarea ? `<textarea id="edit_${name}">${esc(value)}</textarea>` : `<input id="edit_${name}" value="${esc(value)}">`}</div>`;
}
function inspector() {
  const s = state.doc.steps[selected];
  if (!s) {
    $("inspector").innerHTML = "Select a step.";
    return;
  }
  $("inspector").innerHTML =
    `${field("id", s.id)}<div class="field"><label>Operation</label><select id="edit_op">${["OPEN", "BACK", "CLICK", "FILL", "SELECT", "CHECK", "UNCHECK", "HOVER", "PRESS", "UPLOAD", "DOWNLOAD", "WAIT", "ASSERT", "EXTRACT", "SCREENSHOT", "ASSERT_PAGE_STRUCTURE", "CALL"].map((op) => `<option ${s.op === op ? "selected" : ""}>${op}</option>`).join("")}</select></div>${field("target", typeof s.target === "string" ? s.target : JSON.stringify(s.target || {}, null, 2), true)}${field("value", s.url || s.value || "")}${field("assertion", JSON.stringify(s.assertion || { type: "VISIBLE" }, null, 2), true)}${field("timeout", s.timeout ?? "")}${field("state", s.state || "visible")}${field("buffer", s.buffer || "")}${field("command", s.command || "")}<label><input id="edit_breakpoint" type="checkbox" ${s.breakpoint ? "checked" : ""}> Breakpoint before step</label><div class="row"><button id="applyStep" class="primary">Apply</button><button id="parameterize">Parameterize</button><button id="convertAssert">Convert to assertion</button><button id="extract">Extract to buffer</button><button id="waitAfter">Add wait</button></div>`;
  handler("applyStep", async () => {
    const target = $("edit_target").value.trim(),
      op = $("edit_op").value,
      v = $("edit_value").value;
    const next = {
      ...s,
      id: $("edit_id").value,
      op,
      breakpoint: $("edit_breakpoint").checked,
    };
    if (target && target !== "{}")
      next.target = target.startsWith("{") ? JSON.parse(target) : target;
    else delete next.target;
    if (op === "OPEN") {
      next.url = v;
      delete next.value;
    } else {
      next.value = v;
      delete next.url;
    }
    const timeout = $("edit_timeout").value.trim();
    if (timeout) {
      if (!Number.isFinite(Number(timeout)) || Number(timeout) <= 0)
        throw Error("Timeout must be a positive number of milliseconds");
      next.timeout = Number(timeout);
    } else delete next.timeout;
    if (op === "WAIT") next.state = $("edit_state").value || "visible";
    if (op === "ASSERT") next.assertion = JSON.parse($("edit_assertion").value);
    else delete next.assertion;
    if ($("edit_buffer").value) next.buffer = $("edit_buffer").value;
    if (op === "CALL") next.command = $("edit_command").value;
    state.doc.steps[selected] = next;
    await update();
    toast("Step updated.");
  });
  handler("parameterize", async () => {
    const scope = prompt("Scope: R, B or E", "R");
    if (!scope) return;
    if (!/^[RBE]$/.test(scope)) throw Error("Use R, B or E");
    const key = prompt("Parameter key", "Request.VSNR");
    if (key) {
      s.value = `{${scope}[${key}]}`;
      await update();
      inspector();
    }
  });
  handler("convertAssert", async () => {
    s.op = "ASSERT";
    s.assertion = { type: "VISIBLE" };
    delete s.value;
    await update();
    inspector();
  });
  handler("extract", async () => {
    s.op = "EXTRACT";
    s.buffer = prompt("Buffer name", "result") || "result";
    delete s.value;
    await update();
    inspector();
  });
  handler("waitAfter", async () => {
    state.doc.steps.splice(selected + 1, 0, {
      id: "wait_" + Date.now(),
      type: "GUI",
      op: "WAIT",
      target: s.target,
      state: "visible",
    });
    await update();
  });
}
function renderPicked() {
  const p = state.picked;
  if (!p) {
    $("picked").innerHTML = "";
    return;
  }
  const e = p.element;
  $("picked").innerHTML =
    `<h3>${esc(e.name)} <span class="badge">${esc(e.role)}</span></h3><select id="pickedLocator">${e.alternatives.map((a, i) => `<option value="${i}">${esc(a.quality)} · ${a.matches} matches · ${esc(JSON.stringify(a.spec))}</option>`).join("")}</select><p class="hint">Candidates are measured on the current DOM. Prefer a unique locator whose attributes your application keeps stable.</p><select id="pickedAssertion">${["VISIBLE", "HIDDEN", "ENABLED", "DISABLED", "TEXT_EQUALS", "TEXT_CONTAINS", "VALUE", "COUNT"].map((t) => `<option>${t}</option>`).join("")}</select><input id="pickedExpected" value="${esc(e.text || e.value || "")}"><div class="row"><button id="addAssertion">Add assertion</button><button id="replaceLocator">Use for selected step</button></div>`;
  handler("addAssertion", async () => {
    await api("step", {
      step: {
        op: "ASSERT",
        target: e.alternatives[+$("pickedLocator").value].spec,
        assertion: {
          type: $("pickedAssertion").value,
          expected: $("pickedExpected").value,
        },
      },
    });
    toast("Assertion added. Resume recording to continue interacting.");
  });
  handler("replaceLocator", async () => {
    if (selected < 0) throw Error("Select a timeline step");
    state.doc.steps[selected].target =
      e.alternatives[+$("pickedLocator").value].spec;
    await update();
    inspector();
  });
}
function renderRun() {
  const r = state.run || {};
  $("buffers").textContent = JSON.stringify(r.vars?.B || {}, null, 2);
  $("runStatus").textContent = r.status || "idle";
  $("progress").textContent =
    `${r.index || 0} / ${r.total || 0} steps · ${r.runId || "No run yet"}`;
  $("logs").textContent = (r.logs || [])
    .map((l) => `[${l.kind}] ${l.text}`)
    .join("\n");
  $("resultList").innerHTML = (r.results || [])
    .map(
      (s, i) =>
        `<div class="step" data-result="${i}"><span class="${s.status}">${esc(s.status === "passed" ? "✓" : "×")}</span><div class="details"><strong>${esc(s.id)}</strong><small>${esc(s.op)} · ${s.duration} ms</small></div></div>`,
    )
    .join("");
  $("resultList")
    .querySelectorAll("[data-result]")
    .forEach(
      (b) =>
        (b.onclick = () => {
          const s = r.results[+b.dataset.result],
            url = "/runs/" + r.runId + "/";
          $("evidence").innerHTML =
            `<p class="${s.status}">${esc(s.error || "Step passed")}</p>${s.explanation ? `<pre>${esc(JSON.stringify(s.explanation, null, 2))}</pre><select id="repairChoice">${s.explanation.candidates.map((c, i) => `<option value="${i}">${esc(c.name)} · ${c.matches} matches</option>`).join("")}</select><div class="row"><button id="repairOnce">Use once & rerun</button><button id="repairPersist">Update locator & rerun</button></div>` : ""}${s.before ? `<h3>Before</h3><img src="${url + s.before}" alt="Before step">` : ""}${s.after ? `<h3>After</h3><img src="${url + s.after}" alt="After step">` : ""}<a href="${url}${s.index}-before.aria.yaml" target="_blank">DOM accessibility snapshot</a> · <a href="${url}report.json" target="_blank">Report JSON</a> · <a href="${url}trace.zip">Download trace</a>`;
          if (s.explanation?.candidates?.length) {
            for (const [id, persist] of [
              ["repairOnce", false],
              ["repairPersist", true],
            ])
              handler(id, async () => {
                const c = s.explanation.candidates[+$("repairChoice").value];
                if (c.matches !== 1) throw Error("Choose a unique candidate");
                await api("repair", {
                  index: s.index,
                  spec: c.spec,
                  persist,
                  vars: JSON.parse($("vars").value),
                  headed: $("headed").checked,
                });
                toast("Rerun started from beginning.");
              });
          }
        }),
    );
}
handler("record", async () => {
  await api("record/start", {
    url: $("url").value,
    headed: $("headed").checked,
  });
  toast(
    "Browser opened. Record clicks and changes; blur fields to commit input.",
  );
});
handler("stop", () => api("record/stop"));
handler("navigate", () => api("record/navigate", { url: $("url").value }));
handler("pick", () => api("record/mode", { mode: "pick" }));
handler("assertPick", () => api("record/mode", { mode: "assert" }));
handler("normal", () => api("record/mode", { mode: "record" }));
handler("add", () => api("step", { step: { op: "WAIT", value: 500 } }));
handler("autoModules", async () => {
  const r = await api("capture/auto");
  toast(r.note);
});
handler("deleteSelected", async () => {
  state.doc.steps = state.doc.steps.filter((s, i) => !checked.has(i));
  checked.clear();
  selected = -1;
  await update();
  inspector();
});
handler("reusable", async () => {
  if (!checked.size) throw Error("Select steps with checkboxes");
  const indices = [...checked].sort((a, b) => a - b);
  if (indices.some((n, i) => i && n !== indices[i - 1] + 1))
    throw Error("Select a consecutive block");
  const name = prompt("Reusable name", "login");
  if (!name) return;
  state.doc.reusable ||= {};
  if (state.doc.reusable[name]) throw Error("Reusable exists");
  state.doc.reusable[name] = indices.map((i) => state.doc.steps[i]);
  state.doc.steps.splice(indices[0], indices.length, {
    id: "call_" + name + "_" + Date.now(),
    type: "GUI",
    op: "CALL",
    command: name,
  });
  checked.clear();
  selected = -1;
  await update();
});
handler("scan", async () => {
  scan = (await api("capture")).elements;
  $("scanResults").innerHTML = scan
    .map(
      (e, i) =>
        `<div class="scan-item"><div class="row"><input type="checkbox" id="scan_${i}"><input id="key_${i}" value="${esc(
          (e.name || e.role)
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, "_")
            .slice(0, 40) || "element",
        )}_${i}"><strong>${esc(e.name)}</strong><span class="badge">${esc(e.role)}</span><button data-highlight="${i}">Highlight</button></div><select id="locator_${i}">${e.alternatives.map((a, j) => `<option value="${j}">${esc(a.quality)} · ${a.matches} matches · ${esc(JSON.stringify(a.spec))}</option>`).join("")}</select><p class="quality">${esc(e.alternatives[0]?.reason)}</p></div>`,
    )
    .join("");
  $("scanResults")
    .querySelectorAll("[data-highlight]")
    .forEach(
      (b) =>
        (b.onclick = async () => {
          try {
            await api("highlight", {
              spec: scan[+b.dataset.highlight].alternatives[
                +$("locator_" + b.dataset.highlight).value
              ].spec,
            });
          } catch (e) {
            toast(e.message, true);
          }
        }),
    );
});
handler("saveCapture", async () => {
  const elements = scan.flatMap((e, i) =>
    $("scan_" + i).checked
      ? [
          {
            key: $("key_" + i).value,
            spec: e.alternatives[+$("locator_" + i).value].spec,
          },
        ]
      : [],
  );
  if (!elements.length) throw Error("Select elements");
  if (new Set(elements.map((e) => e.key)).size !== elements.length)
    throw Error("Element keys must be unique");
  await api("capture/save", {
    name: $("moduleName").value,
    elements,
    aria: $("aria").checked,
  });
  toast("Page module saved.");
});
handler("health", async () => {
  $("healthResults").innerHTML = (await api("health"))
    .map(
      (e) =>
        `<p><span class="badge">${esc(e.status)}</span> ${esc(e.target)} · ${e.matches} matches</p>`,
    )
    .join("");
});
$("yamlText").oninput = () => (yamlDirty = true);
handler("applyYaml", async () => {
  await api("document", { yaml: $("yamlText").value });
  yamlDirty = false;
  state = await (await fetch("/api/state")).json();
  render();
  toast("YAML valid and applied.");
});
handler("save", async () => {
  await api("project/save", { name: $("projectName").value });
  toast("Project saved.");
});
handler("load", async () => {
  const names = await (await fetch("/api/projects")).json();
  const name = prompt(
    "Available projects: " + names.join(", "),
    $("projectName").value,
  );
  if (name) {
    yamlDirty = false;
    await api("project/load", { name });
    $("projectName").value = name;
    selected = -1;
  }
});
handler("export", () => {
  const a = document.createElement("a");
  a.href = URL.createObjectURL(new Blob([state.yaml], { type: "text/yaml" }));
  a.download = $("projectName").value + ".yaml";
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
});
handler("import", () => {
  $("importFile").click();
});
$("importFile").onchange = async () => {
  try {
    await api("document", { yaml: await $("importFile").files[0].text() });
    yamlDirty = false;
    toast("Imported.");
  } catch (e) {
    toast(e.message, true);
  }
};
async function run(debug) {
  if (yamlDirty) throw Error("Apply YAML changes first");
  const vars = JSON.parse($("vars").value);
  await api("run/start", { headed: $("headed").checked, vars, debug });
  tab("results");
  $("evidence").innerHTML = "";
}
handler("run", () => run(false));
handler("debug", () => run(true));
handler("next", () => api("run/next"));
handler("resume", () => api("run/resume"));
handler("pause", () => api("run/pause"));
handler("cancel", () => api("run/cancel"));
handler("trace", () => api("trace/open"));
const events = new EventSource("/api/events");
events.onmessage = (e) => {
  state = JSON.parse(e.data);
  $("connection").textContent = "● Connected";
  render();
};
events.onerror = () => {
  $("connection").textContent = "Disconnected — reconnecting";
};

handler("refreshRecorderLog", async () => {
  const r = await fetch("/api/record/diagnostics");
  const d = await r.json();
  $("recorderLogStatus").textContent = d.id
    ? `${d.id} · ${d.count} records · latest 300 shown`
    : "Start recording first";
  $("recorderLog").textContent = (d.rows || [])
    .map((r) => JSON.stringify(r))
    .join("\n");
});
