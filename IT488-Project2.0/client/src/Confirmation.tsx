import { useEffect, useRef, useState, type CSSProperties } from "react";

type Props = {
  selectedNames: string[];
  submittedTime: string;
  total?: number;
  onLogAnother: () => void;
  onClearCumulative?: () => void;
  cumulativeNames?: string[];
  rosterTitle?: string;
  classTime?: string;
  professorName?: string;
};

export default function Confirmation({
  selectedNames,
  submittedTime,
  total = 0,
  onLogAnother,
  cumulativeNames,
  onClearCumulative,
  rosterTitle,
  classTime,
  professorName,
}: Props) {
  const [showConfirm, setShowConfirm] = useState(false);
  const confirmRef = useRef<HTMLButtonElement | null>(null);
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

  // Tweak visual style to match the reference confirmation
  const selectedBoxStyle: CSSProperties = {
    border: "1px solid #e0e0e0",
    borderRadius: 10,
    padding: 18,
    backgroundColor: "#fafafa",
    textAlign: "center",
    fontSize: "1rem",
    maxWidth: 340,
    margin: "10px auto",
  };
  const displayedCount =
    Array.isArray(cumulativeNames) && cumulativeNames.length > 0
      ? cumulativeNames.length
      : selectedNames.length;
  const displayPercent =
    total > 0 ? Math.round((displayedCount / total) * 100) : 0;

  function handleDownloadCumulative() {
    // Build rows: one row per timestamp. Prefer structured map in sessionStorage.
    let rows: Array<{ name: string; ts: string }> = [];
    try {
      const raw = sessionStorage.getItem("cumulativeSubmitted");
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
          // object map: name -> [timestamps]
          for (const [name, arr] of Object.entries(
            parsed as Record<string, string[]>,
          )) {
            if (Array.isArray(arr) && arr.length > 0) {
              for (const t of arr) rows.push({ name, ts: t });
            }
          }
        } else if (Array.isArray(parsed)) {
          // legacy array of names -> try to fall back to lastSubmission timestamp
          const rawLast = sessionStorage.getItem("lastSubmission");
          let last: any = null;
          if (rawLast) {
            try {
              last = JSON.parse(rawLast);
            } catch (e) {
              last = null;
            }
          }
          for (const name of parsed) {
            const ts = last?.submittedTime ?? "";
            rows.push({ name, ts });
          }
        }
      }
    } catch (e) {
      // ignore parse errors
    }

    // If no rows from cumulative store, fall back to current submission
    if (rows.length === 0) {
      if (Array.isArray(cumulativeNames) && cumulativeNames.length > 0) {
        for (const n of cumulativeNames) rows.push({ name: n, ts: "" });
      } else if (selectedNames && selectedNames.length > 0) {
        for (const n of selectedNames)
          rows.push({ name: n, ts: submittedTime ?? "" });
      }
    }

    // Sort rows by name then timestamp
    rows.sort((a, b) => {
      const n = a.name.localeCompare(b.name);
      if (n !== 0) return n;
      return (a.ts ?? "").localeCompare(b.ts ?? "");
    });

    // Build metadata block: class title, professor, class time, totals
    const metaLines: string[] = [];
    const title = rosterTitle ?? "";
    const prof = professorName ?? "";
    const cTime = classTime ?? "";
    const loggedCount = rows.length;
    const totalStudents = typeof total === "number" ? total : "";
    const percentLogged =
      typeof total === "number" && total > 0
        ? Math.round((loggedCount / total) * 100)
        : 0;

    // CSV-friendly helpers
    const q = (s: string | number) => `"${String(s).replace(/"/g, '""')}"`;

    metaLines.push(`Class Title,${q(title)}`);
    metaLines.push(`Professor,${q(prof)}`);
    metaLines.push(`Class Time,${q(cTime)}`);
    metaLines.push(`Total Students,${q(totalStudents)}`);
    metaLines.push(`Logged Students,${q(loggedCount)}`);
    metaLines.push(`Percentage,${q(percentLogged + "%")}`);

    const header = "Name,SubmittedTime";
    const csvRows = rows.map((r) => `${q(r.name)},${q(r.ts)}`);
    const csv = [...metaLines, "", header, ...csvRows].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    const filename = `submitted_students.csv`;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }

  useEffect(() => {
    if (showConfirm) {
      confirmRef.current?.focus();
      const onKey = (e: KeyboardEvent) => {
        if (e.key === "Escape") setShowConfirm(false);
      };
      window.addEventListener("keydown", onKey);
      return () => window.removeEventListener("keydown", onKey);
    }
  }, [showConfirm]);

  return (
    <div style={cardStyle}>
      <h2
        style={{
          color: "#00c853",
          marginBottom: 5,
          textAlign: "center",
          fontSize: "1.8rem",
        }}
      >
        Attendance Logged Successfully
      </h2>

      <div
        style={{ textAlign: "center", marginBottom: 12, fontSize: "1.05rem" }}
      >
        <div style={{ marginBottom: 6 }}>
          <strong>Percentage:</strong> {displayedCount} / {total} (
          {displayPercent}%)
        </div>

        <div style={{ marginBottom: 14 }}>
          <strong>Time Submitted:</strong> {submittedTime}
        </div>

        <button
          type="button"
          onClick={onLogAnother}
          style={{
            padding: "8px 12px",
            borderRadius: 4,
            border: "1px solid #222",
            backgroundColor: "#fff",
            cursor: "pointer",
            fontWeight: 600,
          }}
        >
          Log another
        </button>
        {cumulativeNames && cumulativeNames.length > 0 && (
          <button
            type="button"
            onClick={() => setShowConfirm(true)}
            style={{
              marginLeft: 10,
              padding: "8px 12px",
              borderRadius: 4,
              border: "1px solid #c62828",
              backgroundColor: "#fff",
              color: "#c62828",
              cursor: "pointer",
              fontWeight: 600,
            }}
          >
            Clear
          </button>
        )}
      </div>

      <div style={selectedBoxStyle}>
        <div style={{ marginBottom: 10, fontWeight: 700 }}>
          Selected Students:
        </div>
        {selectedNames.map((n) => (
          <div key={n} style={{ marginBottom: 6 }}>
            {n}
          </div>
        ))}
      </div>

      {cumulativeNames && cumulativeNames.length > 0 && (
        <div
          style={{
            marginTop: 16,
            border: "1px solid #eee",
            borderRadius: 8,
            padding: 12,
            backgroundColor: "#fff",
            fontSize: "0.95rem",
            maxWidth: 420,
            margin: "16px auto 0",
          }}
        >
          <div
            style={{ marginBottom: 8, fontWeight: 700, textAlign: "center" }}
          >
            All Submitted Students
            <div style={{ marginTop: 8 }}>
              <button
                onClick={handleDownloadCumulative}
                style={{
                  padding: "6px 10px",
                  borderRadius: 6,
                  border: "1px solid #2a909a",
                  background: "#2a909a",
                  color: "#fff",
                  cursor: "pointer",
                  fontWeight: 600,
                  fontSize: "0.85rem",
                  marginLeft: 8,
                }}
              >
                Download
              </button>
            </div>
          </div>
          {cumulativeNames.map((n) => (
            <div key={n} style={{ marginBottom: 6, textAlign: "center" }}>
              {n}
            </div>
          ))}
        </div>
      )}
      {/* Confirmation modal */}
      {showConfirm && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="clearConfirmTitle"
          style={{
            position: "fixed",
            inset: 0,
            backgroundColor: "rgba(0,0,0,0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 2000,
          }}
          onClick={(e) => {
            if (e.target === e.currentTarget) setShowConfirm(false);
          }}
        >
          <div
            role="document"
            style={{
              width: "90%",
              maxWidth: 420,
              background: "#fff",
              borderRadius: 10,
              padding: 18,
              boxShadow: "0 6px 24px rgba(0,0,0,0.2)",
            }}
          >
            <h3 id="clearConfirmTitle" style={{ marginTop: 0 }}>
              Clear submitted students?
            </h3>
            <p style={{ color: "#444" }}>
              This will remove the list of all submitted students. This cannot
              be undone. Are you sure you want to continue?
            </p>
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: 8,
                marginTop: 12,
              }}
            >
              <button
                onClick={() => setShowConfirm(false)}
                style={{ padding: "8px 12px", borderRadius: 6 }}
              >
                Cancel
              </button>
              <button
                ref={confirmRef}
                onClick={() => {
                  try {
                    sessionStorage.removeItem("cumulativeSubmitted");
                  } catch (e) {
                    // ignore
                  }
                  setShowConfirm(false);
                  if (onClearCumulative) onClearCumulative();
                }}
                style={{
                  padding: "8px 12px",
                  borderRadius: 6,
                  background: "#c62828",
                  color: "#fff",
                  border: "none",
                }}
              >
                Clear
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
