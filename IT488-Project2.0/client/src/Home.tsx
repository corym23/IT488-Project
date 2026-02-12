import { useState, type CSSProperties } from "react";

type Props = {
  onSignIn: () => void;
};

// Mobile-first hero Home with accessible CTA and an info alert for XML tips
export default function Home({ onSignIn }: Props) {
  const [hover, setHover] = useState(false);

  const wrap: CSSProperties = {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    width: "100%",
    minHeight: "60vh",
    padding: "20px",
    boxSizing: "border-box",
  };

  const card: CSSProperties = {
    width: "100%",
    maxWidth: 720,
    padding: "20px",
    borderRadius: 12,
    background: "#fff",
    border: "1px solid #eee",
    boxShadow: "0 2px 10px rgba(0,0,0,0.05)",
    display: "flex",
    flexDirection: "column",
    gap: 16,
    alignItems: "center",
    textAlign: "center",
  };

  // Accessible teal with >=4.5:1 contrast against white
  const ctaColor = "#00695c"; // dark teal

  const ctaStyle: CSSProperties = {
    padding: "14px 20px",
    borderRadius: 12,
    border: "none",
    backgroundColor: ctaColor,
    color: "#fff",
    cursor: "pointer",
    fontWeight: 700,
    fontSize: "1rem",
    transition: "transform 150ms ease, box-shadow 150ms ease",
    boxShadow: hover ? "0 6px 18px rgba(0,0,0,0.12)" : "none",
    transform: hover ? "translateY(-2px)" : "none",
    touchAction: "manipulation",
  };

  const infoBox: CSSProperties = {
    display: "flex",
    alignItems: "flex-start",
    gap: 12,
    maxWidth: 640,
    padding: "12px 14px",
    borderRadius: 8,
    backgroundColor: "#f6fffa",
    border: "1px solid #e6f4ee",
    color: "#235b4b",
    fontSize: "0.95rem",
  };

  const docIcon = (
    <svg
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden
      focusable={false}
    >
      <path
        d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6z"
        fill="#1b5e4b"
      />
      <path d="M14 2v6h6" fill="#47a582" />
    </svg>
  );

  return (
    <div style={wrap}>
      <main style={card} role="main" aria-label="Home">
        <h1 style={{ margin: 0 }}>Welcome To The Attendance Tracking Application!</h1>

        <p
          style={{
            color: "#444",
            lineHeight: 1.5,
            fontSize: "1.05rem",
            margin: 0,
          }}
        >
          
          A simple attendance logging tool that lets instructors quickly mark
          students present and track cumulative submissions.
        </p>

        <div style={{ marginTop: 8 }}>
          <button
            onClick={onSignIn}
            onMouseEnter={() => setHover(true)}
            onMouseLeave={() => setHover(false)}
            style={ctaStyle}
            aria-label="Sign in to Attendance Tracking Application"
          >
            Sign In
          </button>
        </div>

        <div style={infoBox} role="status" aria-live="polite">
          <div aria-hidden>{docIcon}</div>
          <div>
            {/* <strong style={{ display: "block", marginBottom: 6 }}>
              Upload roster (XML)
            </strong> */}
            Upload a custom roster XML from the attendance screen to
            replace or extend the default list of students.
          </div>
        </div>
      </main>
    </div>
  );
}
