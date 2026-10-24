// Runs in each target document. Intentionally uses only browser APIs.
export function installDom() {
  if (window.__agate) return;
  const norm = (s) => (s || "").replace(/\s+/g, " ").trim();
  function role(e) {
    return (
      e.getAttribute("role") ||
      {
        BUTTON: "button",
        A: e.hasAttribute("href") ? "link" : null,
        SELECT: "combobox",
        TEXTAREA: "textbox",
        TABLE: "table",
        TH: "columnheader",
        H1: "heading",
        H2: "heading",
        H3: "heading",
        IMG: "img",
        INPUT:
          {
            checkbox: "checkbox",
            radio: "radio",
            button: "button",
            submit: "button",
            reset: "button",
            image: "button",
            range: "slider",
          }[e.type] || "textbox",
      }[e.tagName]
    );
  }
  function name(e) {
    const refs = e.getAttribute("aria-labelledby");
    return norm(
      e.getAttribute("aria-label") ||
        (refs &&
          refs
            .split(/\s+/)
            .map((id) => document.getElementById(id)?.textContent || "")
            .join(" ")) ||
        (e.labels && [...e.labels].map((l) => l.textContent).join(" ")) ||
        e.getAttribute("alt") ||
        (["BUTTON", "A", "TH", "H1", "H2", "H3"].includes(e.tagName)
          ? e.textContent
          : "") ||
        e.getAttribute("title") ||
        (["submit", "button", "reset"].includes(e.type) ? e.value : ""),
    );
  }
  function css(e) {
    if (e.id) return "#" + CSS.escape(e.id);
    const path = [];
    while (e && e.nodeType === 1 && e !== document.body) {
      let p = e.tagName.toLowerCase();
      const siblings = [...(e.parentElement?.children || [])].filter(
        (x) => x.tagName === e.tagName,
      );
      if (siblings.length > 1) p += `:nth-of-type(${siblings.indexOf(e) + 1})`;
      path.unshift(p);
      e = e.parentElement;
    }
    return "body > " + path.join(" > ");
  }
  function info(e) {
    const r = role(e),
      n = name(e),
      testId = e.getAttribute("data-testid"),
      alternatives = [];
    if (testId)
      alternatives.push({
        spec: { testId },
        quality: "contract",
        reason:
          "Explicit test attribute; stability depends on frontend contract",
      });
    if (r && n)
      alternatives.push({
        spec: { role: r, name: n, exact: true },
        quality: "semantic",
        reason: "Role and accessible name; renaming can break this locator",
      });
    const label = e.labels?.[0] && norm(e.labels[0].textContent);
    if (label)
      alternatives.push({
        spec: { label, exact: true },
        quality: "semantic",
        reason: "Associated label",
      });
    if (e.getAttribute("placeholder"))
      alternatives.push({
        spec: { placeholder: e.getAttribute("placeholder") },
        quality: "semantic",
        reason: "Placeholder can change with locale",
      });
    alternatives.push({
      spec: { css: css(e) },
      quality: e.id ? "id" : "structural",
      reason: e.id
        ? "ID may be generated; check its stability"
        : "DOM structure dependent; review recommended",
    });
    return {
      pageName: norm(
        [...document.querySelectorAll("h2")].find(
          (e) => e.getClientRects().length,
        )?.textContent ||
          document.querySelector("h1")?.textContent ||
          document.title,
      ),
      pageUrl: location.href,
      role: r || e.tagName.toLowerCase(),
      name:
        n ||
        e.getAttribute("placeholder") ||
        e.id ||
        norm(e.textContent).slice(0, 70) ||
        e.tagName.toLowerCase(),
      tag: e.tagName.toLowerCase(),
      password: e.type === "password",
      text: norm(e.textContent).slice(0, 500),
      value: e.type === "password" ? undefined : e.value,
      alternatives,
      target: alternatives[0].spec,
    };
  }
  function all() {
    const found = [];
    const walk = (root) => {
      for (const e of root.querySelectorAll("*")) {
        if (e.shadowRoot) walk(e.shadowRoot);
        if (
          e.matches(
            "input,button,select,textarea,a[href],table,th,h1,h2,h3,[role],[data-testid],[aria-label]",
          ) &&
          e.getClientRects().length &&
          !e.closest("[data-agate-overlay]")
        )
          found.push(info(e));
      }
    };
    walk(document);
    return found;
  }
  const box = document.createElement("div");
  box.dataset.agateOverlay = "true";
  Object.assign(box.style, {
    position: "fixed",
    pointerEvents: "none",
    zIndex: "2147483647",
    border: "2px solid #44d7b6",
    background: "#44d7b61a",
    display: "none",
  });
  function mark(e) {
    if (!box.isConnected) document.documentElement.appendChild(box);
    const b = e.getBoundingClientRect();
    Object.assign(box.style, {
      display: "block",
      left: b.x + "px",
      top: b.y + "px",
      width: b.width + "px",
      height: b.height + "px",
    });
  }
  let mode = "record";
  const documentId = crypto.randomUUID();
  let sequence = 0,
    pointer = null;
  const describe = (e) => ({
    tag: e?.tagName?.toLowerCase(),
    id: e?.id,
    type: e?.type,
    role: e?.tagName ? role(e) : undefined,
    name: e?.tagName ? name(e) : undefined,
    value: e?.type === "password" ? "<masked>" : e?.value,
    options:
      e?.tagName === "SELECT"
        ? [...e.selectedOptions].map((o) => ({
            label: norm(o.textContent),
            value: o.value,
          }))
        : undefined,
  });
  const metadata = (ev, e) => ({
    documentId,
    sourceSequence: ++sequence,
    sourceTime: Date.now(),
    eventTime: ev?.timeStamp,
    eventType: ev?.type,
    isTrusted: ev?.isTrusted,
    detail: ev?.detail,
    url: location.href,
    title: document.title,
    readyState: document.readyState,
    activeElement: describe(document.activeElement),
    eventElement: describe(e),
    pointer,
  });
  const send = (x, ev, e) =>
    window.agateEvent?.({ ...x, meta: metadata(ev, e) }).catch(() => {});
  const diagnostic = (ev, e, outcome) =>
    send({ kind: "diagnostic", outcome }, ev, e);
  document.addEventListener(
    "pointerdown",
    (ev) => {
      if (mode !== "record") return;
      const e = element(ev);
      pointer = {
        time: Date.now(),
        element: describe(e),
        isTrusted: ev.isTrusted,
      };
      diagnostic(ev, e, "pointerdown");
    },
    true,
  );
  document.addEventListener(
    "DOMContentLoaded",
    (ev) => diagnostic(ev, null, "document-ready"),
    true,
  );
  window.addEventListener("pageshow", (ev) => diagnostic(ev, null, "pageshow"));
  const element = (ev) => {
    const e = ev.composedPath()[0];
    return (
      e?.closest?.("button,a,input,textarea,select,[role],[data-testid]") || e
    );
  };
  document.addEventListener(
    "mousemove",
    (ev) => {
      if (mode === "pick" || mode === "assert") mark(element(ev));
    },
    true,
  );
  document.addEventListener(
    "click",
    (ev) => {
      const e = element(ev);
      if (!e?.tagName || e.closest("[data-agate-overlay]")) return;
      if (mode === "pick" || mode === "assert") {
        ev.preventDefault();
        ev.stopImmediatePropagation();
        send({ kind: mode, element: info(e) }, ev, e);
        return;
      }
      if (mode !== "record") return;
      if (!ev.isTrusted) {
        diagnostic(ev, e, "ignored-synthetic-click");
        return;
      }
      // Text/select/check controls are recorded through input/change.
      // Submit/button/image/reset inputs must retain their CLICK event.
      if (
        e.matches("textarea,select") ||
        (e.tagName === "INPUT" &&
          !["submit", "button", "image", "reset"].includes(e.type))
      ) {
        diagnostic(ev, e, "ignored-control-click");
        return;
      }
      send({ kind: "action", op: "CLICK", element: info(e) }, ev, e);
    },
    true,
  );
  document.addEventListener(
    "input",
    (ev) => {
      if (mode !== "record") return;
      const e = ev.composedPath()[0];
      if (e.tagName === "SELECT") {
        send(
          { kind: "action", op: "SELECT", value: e.value, element: info(e) },
          ev,
          e,
        );
        return;
      }
      if (
        e.matches("textarea,input") &&
        !["checkbox", "radio", "file", "range", "button", "submit"].includes(
          e.type,
        )
      )
        send(
          {
            kind: "action",
            op: "FILL",
            value: e.type === "password" ? "{E[PASSWORD]}" : e.value,
            element: info(e),
          },
          ev,
          e,
        );
    },
    true,
  );
  document.addEventListener(
    "change",
    (ev) => {
      if (mode !== "record") return;
      const e = ev.composedPath()[0];
      if (e.type === "file") {
        send(
          {
            kind: "action",
            op: "UPLOAD",
            value: "<SET_LOCAL_FILE_PATH>",
            element: info(e),
          },
          ev,
          e,
        );
        return;
      }
      const op =
        e.tagName === "SELECT"
          ? "SELECT"
          : e.type === "checkbox" || e.type === "radio"
            ? e.checked
              ? "CHECK"
              : "UNCHECK"
            : "FILL";
      send(
        {
          kind: "action",
          op,
          value: e.type === "password" ? "{E[PASSWORD]}" : e.value,
          element: info(e),
        },
        ev,
        e,
      );
    },
    true,
  );
  document.addEventListener(
    "keydown",
    (ev) => {
      if (mode === "record" && ["Enter", "Tab", "Escape"].includes(ev.key)) {
        const e = ev.composedPath()[0];
        if (e.matches("input,textarea") && ev.key === "Enter")
          send(
            {
              kind: "action",
              op: "FILL",
              value: e.type === "password" ? "{E[PASSWORD]}" : e.value,
              element: info(e),
            },
            ev,
            e,
          );
        send(
          { kind: "action", op: "PRESS", value: ev.key, element: info(e) },
          ev,
          e,
        );
      }
    },
    true,
  );
  window.__agate = {
    all,
    info,
    setMode: (m) => {
      mode = m;
      box.style.display = "none";
    },
    highlight: (spec) => {
      let e;
      if (spec.css) e = document.querySelector(spec.css);
      else if (spec.testId)
        e = [...document.querySelectorAll("[data-testid]")].find(
          (e) => e.dataset.testid === spec.testId,
        );
      else
        e = [...document.querySelectorAll("*")].find(
          (e) => role(e) === spec.role && name(e) === spec.name,
        );
      if (e) mark(e);
    },
  };
}
