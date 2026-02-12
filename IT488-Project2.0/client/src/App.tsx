import {
  useEffect,
  useRef,
  useState,
  type FormEvent,
  type CSSProperties,
} from "react";
import Logon from "./Logon";
import Confirmation from "./Confirmation";
import Home from "./Home";

function App() {
  // Roster names from XML
  const [names, setNames] = useState<string[]>([]);

  // Search text for filtering names
  const [searchText, setSearchText] = useState("");

  // Radio selection
  const [selectedNames, setSelectedNames] = useState<string[]>([]);

  // Success screen state (time kept for storage)
  const [submitted, setSubmitted] = useState(false);
  const [submittedTime, setSubmittedTime] = useState("");

  // Track current path so we can render confirmation at /confirmation
  const [path, setPath] = useState(window.location.pathname);

  const [shouldFocusSearch, setShouldFocusSearch] = useState(false);
  const [loggedIn, setLoggedIn] = useState(false);
  const [showLogon, setShowLogon] = useState(false);
  const [cumulativeVersion, setCumulativeVersion] = useState(0);
  const [loggedToday, setLoggedToday] = useState<Record<string, boolean>>({});
  const [uploadIncomingNames, setUploadIncomingNames] = useState<
    string[] | null
  >(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadAction, setUploadAction] = useState<"merge" | "overwrite">(
    "merge",
  );
  const [uploadIncomingTitle, setUploadIncomingTitle] = useState<string | null>(
    null,
  );
  const [uploadIncomingClassTime, setUploadIncomingClassTime] = useState<
    string | null
  >(null);

  // Ensure app reflects route in the URL for home/login/attendance
  useEffect(() => {
    try {
      if (!loggedIn) {
        if (showLogon) {
          history.replaceState({}, "", "/login");
          setPath("/login");
        } else {
          history.replaceState({}, "", "/home");
          setPath("/home");
        }
      } else {
        // when logged in, keep user on attendance route unless explicitly on confirmation
        const p = window.location.pathname;
        if (p === "/home" || p === "/login" || p === "/") {
          history.replaceState({}, "", "/attendance");
          setPath("/attendance");
        } else {
          setPath(p);
        }
      }
    } catch (e) {
      // ignore history errors in some environments
    }
  }, [loggedIn, showLogon]);

  // Roster title from XML
  const [rosterTitle, setRosterTitle] = useState("");
  const [, setRosterStatus] = useState<"loading" | "ready" | "error">(
    "loading",
  );

  // Class time from XML
  const [classTime, setClassTime] = useState("");

  // Error message
  const [message, setMessage] = useState("");

  const searchRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    function handleMouseDown() {
      document.body.classList.remove("keyboard-nav");
    }

    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === "Tab") {
        document.body.classList.add("keyboard-nav");
      }
    }

    window.addEventListener("mousedown", handleMouseDown);
    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("mousedown", handleMouseDown);
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, []);

  // Load roster from XML
  useEffect(() => {
    setRosterStatus("loading");

    fetch(`${import.meta.env.BASE_URL}roster.xml`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("HTTP " + response.status);
        }
        return response.text();
      })
      .then((xmlText) => {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(xmlText, "application/xml");

        const parseError = xmlDoc.getElementsByTagName("parsererror")[0];
        if (parseError) {
          setMessage("Roster XML parse error. Check roster.xml format.");
          setRosterTitle("");
          setNames([]);
          return;
        }

        // Title
        const titleNode = xmlDoc.getElementsByTagName("title")[0];
        let loadedTitle = titleNode?.textContent?.trim() || "Roster";

        // Class time
        const timeNode = xmlDoc.getElementsByTagName("classTime")[0];
        let loadedTime = timeNode?.textContent?.trim() || "";

        // If user previously uploaded a roster, prefer stored roster metadata
        try {
          const metaRaw = sessionStorage.getItem("rosterMeta");
          if (metaRaw) {
            const meta = JSON.parse(metaRaw);
            if (meta?.title) loadedTitle = String(meta.title);
            if (meta?.classTime) loadedTime = String(meta.classTime);
          }
        } catch (e) {
          // ignore parse errors and keep loaded values from file
        }

        setRosterTitle(loadedTitle);
        setClassTime(loadedTime);

        // Names
        const nameElements = xmlDoc.getElementsByTagName("name");
        const loadedNames: string[] = [];

        for (let i = 0; i < nameElements.length; i++) {
          const text = nameElements[i].textContent;
          if (text && text.trim()) loadedNames.push(text.trim());
        }

        loadedNames.sort((a, b) => a.localeCompare(b));
        setNames(loadedNames);

        setRosterStatus("ready");
      })
      .catch((err) => {
        setRosterStatus("error");
        setMessage("Error loading roster: " + String(err));
      });
  }, []);

  // Load cumulative submitted map into state so UI can disable already-logged names
  useEffect(() => {
    try {
      const raw = sessionStorage.getItem("cumulativeSubmitted");
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) {
          // legacy array -> initialize empty arrays but do not mark as logged today
          const m: Record<string, string[]> = {};
          for (const n of parsed) m[n] = [];
          // nothing logged today if there are no timestamps
          setLoggedToday({});
        } else if (parsed && typeof parsed === "object") {
          const map = parsed as Record<string, string[]>;
          // compute which names have a timestamp for today's date
          const today = new Date();
          const tYear = today.getFullYear();
          const tMonth = today.getMonth();
          const tDate = today.getDate();
          const todayMap: Record<string, boolean> = {};
          for (const [name, arr] of Object.entries(map)) {
            if (Array.isArray(arr)) {
              for (const ts of arr) {
                try {
                  const d = new Date(ts);
                  if (
                    d.getFullYear() === tYear &&
                    d.getMonth() === tMonth &&
                    d.getDate() === tDate
                  ) {
                    todayMap[name] = true;
                    break;
                  }
                } catch (e) {
                  // ignore parse
                }
              }
            }
          }
          setLoggedToday(todayMap);
        } else {
          setLoggedToday({});
        }
      } else {
        setLoggedToday({});
      }
    } catch (e) {
      setLoggedToday({});
    }
  }, [cumulativeVersion]);

  useEffect(() => {
    if (shouldFocusSearch) {
      setTimeout(() => {
        searchRef.current?.focus();
      }, 0);
      setShouldFocusSearch(false);
    }
  }, [shouldFocusSearch]);

  // keep path state in sync with history navigation
  useEffect(() => {
    const onPop = () => setPath(window.location.pathname);
    window.addEventListener("popstate", onPop);
    return () => window.removeEventListener("popstate", onPop);
  }, []);

  function toggleName(name: string) {
    // Clear any inline validation message when the user selects/unselects a name
    setMessage("");

    setSelectedNames((prev) => {
      if (prev.includes(name)) {
        return prev.filter((n) => n !== name); // uncheck
      }
      return [...prev, name]; // check
    });
  }

  const filteredNames = names.filter((name) =>
    name.toLowerCase().includes(searchText.toLowerCase()),
  );

  const displayRosterTitle =
    rosterTitle && rosterTitle.trim() ? rosterTitle : "Roster";

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setMessage("");

    // If user did nothing
    if (!searchText.trim() && selectedNames.length === 0) {
      setMessage("please enter or select a name from the list.");
      return;
    }

    // If they typed but didn't select anything
    if (selectedNames.length === 0) {
      setMessage("Name Not Found");
      return;
    }

    const timestamp = new Date()
      .toISOString()
      .replace("T", " ")
      .substring(0, 19);
    // Load cumulative map from sessionStorage. Support legacy array format.
    let cumulativeMap: Record<string, string[]> = {};
    try {
      const raw = sessionStorage.getItem("cumulativeSubmitted");
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) {
          // legacy: array of names -> convert to map with empty arrays
          for (const n of parsed) cumulativeMap[n] = [];
        } else if (parsed && typeof parsed === "object") {
          cumulativeMap = parsed as Record<string, string[]>;
        }
      }
    } catch (e) {
      cumulativeMap = {};
    }

    // Prevent duplicate logins: skip any names already logged today in cumulativeMap
    const hasLoggedToday = (arr?: string[] | undefined) => {
      if (!Array.isArray(arr) || arr.length === 0) return false;
      try {
        const today = new Date();
        const y = today.getFullYear();
        const m = today.getMonth();
        const d = today.getDate();
        for (const ts of arr) {
          const dt = new Date(ts);
          if (
            dt.getFullYear() === y &&
            dt.getMonth() === m &&
            dt.getDate() === d
          )
            return true;
        }
      } catch (e) {
        // ignore
      }
      return false;
    };

    const skipped = selectedNames.filter((n) =>
      hasLoggedToday(cumulativeMap[n]),
    );
    const toLog = selectedNames.filter((n) => !skipped.includes(n));

    if (toLog.length === 0) {
      setMessage(
        skipped.length === 1
          ? `${skipped[0]} was already logged`
          : `Selected students already logged: ${skipped.join(", ")}`,
      );
      return;
    }

    // Append timestamp for each name that isn't already logged
    for (const n of toLog) {
      if (!Array.isArray(cumulativeMap[n])) cumulativeMap[n] = [];
      cumulativeMap[n].push(new Date().toISOString());
    }

    const payload = {
      selectedNames: toLog,
      submittedTime: timestamp,
    };

    // Try sending to server API (falls back to local-only if network fails)
    try {
      const apiBase = import.meta.env.VITE_API_BASE || "http://localhost:4000";
      const res = await fetch(`${apiBase}/api/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const resJson = await res.json().catch(() => null);
      if (!res.ok) {
        // show a non-blocking message but continue to confirmation
        setMessage(
          "Submission failed: " + (resJson?.message || String(res.status)),
        );
      }
    } catch (e) {
      console.warn("Submission API call failed", e);
      setMessage("Submission saved locally (network error).");
    }

    try {
      sessionStorage.setItem("lastSubmission", JSON.stringify(payload));
      sessionStorage.setItem(
        "cumulativeSubmitted",
        JSON.stringify(cumulativeMap),
      );
      // bump version so UI reloads loggedMap and disables newly-logged names
      setCumulativeVersion((v) => v + 1);
    } catch (e) {
      // ignore
    }

    history.pushState(payload, "", "/confirmation");
    setSubmittedTime(timestamp);
    setSubmitted(true);
    setPath(window.location.pathname);
  }

  function handleLogAnother() {
    // Clear stored submission and navigate back to base
    try {
      sessionStorage.removeItem("lastSubmission");
    } catch (e) {
      // ignore
    }
    history.pushState({}, "", "/attendance");
    setSubmitted(false);
    setSubmittedTime("");
    setSearchText("");
    setMessage("");
    setShouldFocusSearch(true);
    setSelectedNames([]);
    setPath(window.location.pathname);
  }

  // Shared card style
  const cardStyle: CSSProperties = {
    width: "100%",
    maxWidth: 500,
    margin: "0 auto",
    padding: 20,
    border: "1px solid #ccc",
    borderRadius: 12,
    boxSizing: "border-box",
    backgroundColor: "#fff",
  };

  const contentWrapStyle: React.CSSProperties = {
    width: "100%",
    maxWidth: 500,
    margin: "0 auto",
    boxSizing: "border-box",
  };

  // Top nav style
  const navStyle: React.CSSProperties = {
    width: "100%",
    backgroundColor: "#2a909a",
    padding: "10px 10px",
    boxSizing: "border-box",
    color: "#f5f5f5",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
  };

  function handleLogout() {
    try {
      sessionStorage.removeItem("lastSubmission");
      sessionStorage.removeItem("cumulativeSubmitted");
    } catch (e) {
      // ignore
    }
    setLoggedIn(false);
    setSubmitted(false);
    setSubmittedTime("");
    setSearchText("");
    setSelectedNames([]);
    setMessage("");
    history.pushState({}, "", "/home");
    setPath("/home");
  }

  if (!loggedIn) {
    return showLogon ? (
      <Logon
        onLogin={() => {
          setLoggedIn(true);
          setShowLogon(false);
          try {
            history.pushState({}, "", "/attendance");
            setPath("/attendance");
          } catch (e) {
            // ignore
          }
        }}
      />
    ) : (
      <Home onSignIn={() => setShowLogon(true)} />
    );
  }

  return (
    <div style={{ width: "100%" }}>
      {/* NAV BAR */}
      <nav style={navStyle}>
        <h1
          style={{
            margin: 0,
            color: "#fff",
            fontSize: "1.8rem",
            fontWeight: 600,
            textAlign: "left",
          }}
        >
          Attendance Tracking Application
        </h1>
        <div style={{ marginLeft: 12 }}>
          <button
            onClick={handleLogout}
            style={{
              background: "transparent",
              color: "#fff",
              border: "1px solid rgba(255,255,255,0.2)",
              padding: "6px 10px",
              borderRadius: 6,
              cursor: "pointer",
              fontWeight: 600,
            }}
          >
            Logout
          </button>
        </div>
      </nav>

      {/* PAGE CONTENT */}
      <div style={{ marginTop: 30, paddingBottom: 40 }}>
        <div style={contentWrapStyle}>
          {/* ROSTER TITLE (from XML) */}
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              marginBottom: 16,
              overflow: "visible",
            }}
          >
            <div
              style={{
                display: "inline-flex",
                alignItems: "baseline",
                gap: 14,
                whiteSpace: "nowrap",
                maxWidth: "none",
              }}
            >
              <div
                style={{
                  fontWeight: 700,
                  fontSize: "2rem",
                  color: "#222",
                }}
              >
                {displayRosterTitle}
              </div>

              {classTime && (
                <div
                  style={{
                    fontWeight: 600,
                    fontSize: "1.1rem",
                    color: "#555",
                  }}
                >
                  {classTime}
                </div>
              )}
            </div>
          </div>

          {/* CONFIRMATION ROUTE */}
          {window.location.pathname.endsWith("/confirmation") ||
          path.endsWith("/confirmation") ||
          submitted ? (
            (() => {
              const histState: any = history.state ?? null;
              let submission: {
                selectedNames?: string[];
                submittedTime?: string;
              } | null = null;
              if (
                histState &&
                histState.selectedNames &&
                histState.submittedTime
              ) {
                submission = histState;
              } else {
                try {
                  const s = sessionStorage.getItem("lastSubmission");
                  if (s) submission = JSON.parse(s);
                } catch (e) {
                  submission = null;
                }
              }

              let cumulativeFromState: string[] | undefined = undefined;
              if (
                histState &&
                (histState.cumulative || histState.cumulativeSubmitted)
              ) {
                const candidate =
                  histState.cumulative ?? histState.cumulativeSubmitted;
                if (Array.isArray(candidate))
                  cumulativeFromState = candidate as string[];
                else if (candidate && typeof candidate === "object")
                  cumulativeFromState = Object.keys(
                    candidate as Record<string, any>,
                  );
              } else {
                try {
                  const raw = sessionStorage.getItem("cumulativeSubmitted");
                  if (raw) {
                    const parsed = JSON.parse(raw);
                    if (Array.isArray(parsed))
                      cumulativeFromState = parsed as string[];
                    else if (parsed && typeof parsed === "object")
                      cumulativeFromState = Object.keys(
                        parsed as Record<string, any>,
                      );
                  }
                } catch (e) {
                  cumulativeFromState = undefined;
                }
              }

              return (
                <Confirmation
                  selectedNames={submission?.selectedNames ?? selectedNames}
                  submittedTime={submission?.submittedTime ?? submittedTime}
                  total={names.length}
                  cumulativeNames={cumulativeFromState}
                  rosterTitle={rosterTitle}
                  classTime={classTime}
                  // derive professor name from title if it contains "Professor ..."
                  professorName={(() => {
                    try {
                      const m = (rosterTitle || "").match(
                        /Professor\s+([^<\n,]+)/i,
                      );
                      if (m && m[1])
                        return m[1].replace(/'s\s*Class$/i, "").trim();
                    } catch (e) {
                      /* ignore */
                    }
                    return "";
                  })()}
                  onClearCumulative={() => {
                    try {
                      sessionStorage.removeItem("cumulativeSubmitted");
                    } catch (e) {
                      // ignore
                    }
                    setCumulativeVersion((v) => v + 1);
                    // navigate back to attendance (refresh submission page)
                    try {
                      history.pushState({}, "", "/attendance");
                    } catch (e) {
                      // ignore
                    }
                    setSubmitted(false);
                    setSubmittedTime("");
                    setSearchText("");
                    setSelectedNames([]);
                    setPath("/attendance");
                  }}
                  onLogAnother={handleLogAnother}
                />
              );
            })()
          ) : (
            // INITIAL SUBMISSION SCREEN
            <form onSubmit={handleSubmit} style={cardStyle}>
              <label
                style={{
                  display: "block",
                  marginBottom: 8,
                  fontSize: "1.4rem",
                }}
              >
                Type Student Name
              </label>

              <input
                ref={searchRef}
                type="text"
                placeholder="Type name..."
                value={searchText}
                onChange={(e) => {
                  setSearchText(e.target.value);
                  setMessage("");
                }}
                aria-invalid={!!message}
                aria-describedby={message ? "error-message" : undefined}
                style={{
                  width: "100%",
                  padding: "12px 10px",
                  lineHeight: "1",
                  boxSizing: "border-box",
                  borderRadius: 18,
                  border: "2.5px solid #2a909a",
                  backgroundColor: "#fff",
                  boxShadow: "none",
                  outline: "none",
                  appearance: "none",
                  WebkitAppearance: "none",
                  MozAppearance: "none",
                  backgroundClip: "padding-box",
                }}
              />

              {/* inline validation message */}
              {message ? (
                <p
                  id="error-message"
                  role="alert"
                  style={{ color: "crimson", marginTop: 8 }}
                >
                  {message}
                </p>
              ) : null}

              {filteredNames.length > 0 && (
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    margin: "12px 0",
                  }}
                >
                  {/* LEFT: OR text */}
                  <p
                    style={{
                      margin: 0,
                      color: "#666",
                      fontSize: "0.9rem",
                      fontWeight: 500,
                      textTransform: "uppercase",
                    }}
                  >
                    OR Select The Name
                  </p>

                  {/* RIGHT: Roster actions */}
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: 5,
                      fontSize: "0.7rem",
                    }}
                  >
                    {/* Upload */}
                    <label
                      style={{
                        cursor: "pointer",
                        color: "#2a909a",
                        fontSize: "0.7rem",
                      }}
                    >
                      Upload
                      <input
                        type="file"
                        accept=".xml"
                        style={{ display: "none" }}
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (!file) return;

                          const reader = new FileReader();
                          reader.onload = () => {
                            try {
                              const text = String(reader.result ?? "");
                              const parser = new DOMParser();
                              const xml = parser.parseFromString(
                                text,
                                "application/xml",
                              );
                              const titleNode =
                                xml.getElementsByTagName("title")[0];
                              const loadedTitle =
                                titleNode?.textContent?.trim() || "Roster";
                              const timeNode =
                                xml.getElementsByTagName("classTime")[0];
                              const loadedTime =
                                timeNode?.textContent?.trim() || "";
                              const nameEls = xml.getElementsByTagName("name");
                              const incoming: string[] = [];
                              for (let i = 0; i < nameEls.length; i++) {
                                const t = nameEls[i].textContent;
                                if (t && t.trim()) incoming.push(t.trim());
                              }
                              // dedupe and sort
                              const uniq = Array.from(new Set(incoming)).sort(
                                (a, b) => a.localeCompare(b),
                              );

                              // Check existing cumulativeSubmitted
                              let existingCount = 0;
                              try {
                                const raw = sessionStorage.getItem(
                                  "cumulativeSubmitted",
                                );
                                if (raw) {
                                  const parsed = JSON.parse(raw);
                                  if (Array.isArray(parsed))
                                    existingCount = parsed.length;
                                  else if (parsed && typeof parsed === "object")
                                    existingCount = Object.keys(parsed).length;
                                }
                              } catch (er) {
                                existingCount = 0;
                              }

                              if (existingCount > 0) {
                                // show modal asking merge/overwrite
                                setUploadIncomingNames(uniq);
                                setUploadIncomingTitle(loadedTitle);
                                setUploadIncomingClassTime(loadedTime);
                                setUploadAction("merge");
                                setShowUploadModal(true);
                              } else {
                                // nothing to conflict with — write incoming as cumulativeSubmitted
                                const map: Record<string, string[]> = {};
                                // initialize incoming students with empty timestamp arrays (not auto-logged)
                                for (const n of uniq) map[n] = [];
                                try {
                                  sessionStorage.setItem(
                                    "cumulativeSubmitted",
                                    JSON.stringify(map),
                                  );
                                  setCumulativeVersion((v) => v + 1);
                                  // update displayed roster to the uploaded roster
                                  setNames(uniq);
                                  setRosterTitle(loadedTitle);
                                  setClassTime(loadedTime);
                                  // persist uploaded roster metadata so it survives reloads
                                  try {
                                    sessionStorage.setItem(
                                      "rosterMeta",
                                      JSON.stringify({
                                        title: loadedTitle,
                                        classTime: loadedTime,
                                      }),
                                    );
                                  } catch (e) {
                                    // ignore
                                  }
                                } catch (e) {
                                  // ignore
                                }
                              }
                            } catch (e) {
                              console.warn(
                                "Failed to parse uploaded roster",
                                e,
                              );
                            }
                          };
                          reader.readAsText(file);
                        }}
                      />
                    </label>
                  </div>
                </div>
              )}
              {/* Upload conflict modal */}
              {showUploadModal && uploadIncomingNames && (
                <div
                  role="dialog"
                  aria-modal="true"
                  style={{
                    position: "fixed",
                    inset: 0,
                    backgroundColor: "rgba(0,0,0,0.35)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    zIndex: 2200,
                  }}
                  onClick={(e) => {
                    if (e.target === e.currentTarget) setShowUploadModal(false);
                  }}
                >
                  <div
                    style={{
                      width: "92%",
                      maxWidth: 520,
                      background: "#fff",
                      borderRadius: 10,
                      padding: 18,
                    }}
                  >
                    <h3 style={{ marginTop: 0 }}>Upload conflict</h3>
                    <p style={{ color: "#444" }}>
                      An attendance list already exists. How would you like to
                      combine the uploaded roster with the current logged
                      attendance?
                    </p>
                    <div style={{ display: "flex", gap: 8, marginTop: 8 }}>
                      <label
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                        }}
                      >
                        <input
                          type="radio"
                          name="uploadAction"
                          checked={uploadAction === "merge"}
                          onChange={() => setUploadAction("merge")}
                        />
                        Merge (add new students, keep existing)
                      </label>
                      <label
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                        }}
                      >
                        <input
                          type="radio"
                          name="uploadAction"
                          checked={uploadAction === "overwrite"}
                          onChange={() => setUploadAction("overwrite")}
                        />
                        Overwrite (replace current attendance)
                      </label>
                    </div>

                    <div
                      style={{
                        display: "flex",
                        justifyContent: "flex-end",
                        gap: 8,
                        marginTop: 14,
                      }}
                    >
                      <button
                        onClick={() => {
                          setShowUploadModal(false);
                          setUploadIncomingNames(null);
                        }}
                        style={{ padding: "8px 12px", borderRadius: 6 }}
                      >
                        Cancel
                      </button>
                      <button
                        onClick={() => {
                          try {
                            const raw = sessionStorage.getItem(
                              "cumulativeSubmitted",
                            );
                            let existing: Record<string, string[]> = {};
                            if (raw) {
                              const parsed = JSON.parse(raw);
                              if (Array.isArray(parsed)) {
                                for (const n of parsed) existing[n] = [];
                              } else if (parsed && typeof parsed === "object")
                                existing = parsed as Record<string, string[]>;
                            }

                            // no auto-logging on merge/overwrite; timestamp not needed here
                            const incoming = uploadIncomingNames ?? [];

                            if (uploadAction === "overwrite") {
                              // overwrite: replace current attendance map with incoming students, but do not auto-log timestamps
                              const map: Record<string, string[]> = {};
                              for (const n of incoming) map[n] = [];
                              sessionStorage.setItem(
                                "cumulativeSubmitted",
                                JSON.stringify(map),
                              );
                              // replace displayed roster with uploaded
                              setNames(
                                incoming
                                  .slice()
                                  .sort((a, b) => a.localeCompare(b)),
                              );
                              setRosterTitle(uploadIncomingTitle ?? "Roster");
                              setClassTime(uploadIncomingClassTime ?? "");
                              // persist uploaded roster metadata
                              try {
                                sessionStorage.setItem(
                                  "rosterMeta",
                                  JSON.stringify({
                                    title: uploadIncomingTitle ?? "Roster",
                                    classTime: uploadIncomingClassTime ?? "",
                                  }),
                                );
                              } catch (e) {
                                // ignore
                              }
                            } else {
                              // merge: copy existing and add any missing names with empty timestamp arrays; preserve existing timestamp arrays
                              const map = { ...existing } as Record<
                                string,
                                string[]
                              >;
                              for (const n of incoming) {
                                if (
                                  !Object.prototype.hasOwnProperty.call(map, n)
                                ) {
                                  map[n] = [];
                                }
                              }
                              sessionStorage.setItem(
                                "cumulativeSubmitted",
                                JSON.stringify(map),
                              );
                              // update displayed roster to include uploaded names (union)
                              setNames((prev) => {
                                const merged = Array.from(
                                  new Set([...(prev || []), ...incoming]),
                                );
                                return merged.sort((a, b) =>
                                  a.localeCompare(b),
                                );
                              });
                              setRosterTitle(
                                uploadIncomingTitle ?? rosterTitle,
                              );
                              setClassTime(
                                uploadIncomingClassTime ?? classTime,
                              );
                              // persist uploaded roster metadata (merge keeps prior if incoming null)
                              try {
                                sessionStorage.setItem(
                                  "rosterMeta",
                                  JSON.stringify({
                                    title: uploadIncomingTitle ?? rosterTitle,
                                    classTime:
                                      uploadIncomingClassTime ?? classTime,
                                  }),
                                );
                              } catch (e) {
                                // ignore
                              }
                            }
                            setCumulativeVersion((v) => v + 1);
                          } catch (err) {
                            console.warn("upload merge/overwrite failed", err);
                          }
                          setShowUploadModal(false);
                          setUploadIncomingNames(null);
                        }}
                        style={{
                          padding: "8px 12px",
                          borderRadius: 6,
                          background: "#2a909a",
                          color: "#fff",
                          border: "none",
                        }}
                      >
                        Confirm
                      </button>
                    </div>
                  </div>
                </div>
              )}
              <div
                role="radiogroup"
                aria-describedby={message ? "error-message" : undefined}
                style={{
                  marginTop: 10,
                  width: "100%",
                  boxSizing: "border-box",
                  border: "1px solid #ccc",
                  borderRadius: 10,
                  padding: 12,
                  backgroundColor: "#f9f9f9",
                  maxHeight: 200,
                  overflowY: "auto",
                }}
              >
                {filteredNames.length === 0 ? (
                  <div style={{ color: "#666", fontSize: "0.95rem" }}>
                    No matching names.
                  </div>
                ) : (
                  filteredNames.map((name) => {
                    const already = Boolean(loggedToday[name]);
                    return (
                      <div key={name} style={{ marginBottom: 8 }}>
                        <label>
                          <input
                            type="checkbox"
                            checked={selectedNames.includes(name)}
                            onChange={() => toggleName(name)}
                            disabled={already}
                          />
                          {name}{" "}
                          {already ? (
                            <span style={{ color: "#666" }}>
                              (already logged)
                            </span>
                          ) : null}
                        </label>
                      </div>
                    );
                  })
                )}
              </div>

              <div style={{ textAlign: "center", marginTop: 15 }}>
                <button
                  type="submit"
                  style={{
                    padding: "10px 14px",
                    borderRadius: 5,
                    border: "1px solid #222",
                    backgroundColor: "#f9f9f9",
                    cursor: "pointer",
                    fontWeight: 600,
                  }}
                >
                  Submit
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
