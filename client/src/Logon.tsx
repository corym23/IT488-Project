import { useState, type CSSProperties } from "react";

type Props = {
    onLogin: () => void;
};

export default function Logon({ onLogin }: Props) {
    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const ALLOWED_PASSWORD = "IT488";
    const ALLOWED_NAME = "Ahmad Kassem";

    const navStyle: CSSProperties = {
        width: "100%",
        backgroundColor: "#2a909a",
        padding: "10px 10px",
        boxSizing: "border-box",
        color: "#f5f5f5",
    };

    const contentWrapStyle: CSSProperties = {
        width: "100%",
        maxWidth: 500,
        margin: "0 auto",
        boxSizing: "border-box",
    };

    const cardStyle: CSSProperties = {
        width: "100%",
        maxWidth: 500,
        margin: "20px auto",
        padding: 20,
        border: "1px solid #ccc",
        borderRadius: 12,
        boxSizing: "border-box",
        backgroundColor: "#fff",
        textAlign: "left",
    };

    const centerStyle: CSSProperties = { textAlign: "center" };

    return (
        <div style={{ width: "100%" }}>
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
            </nav>

            <div style={{ marginTop: 30, paddingBottom: 40 }}>
                <div style={contentWrapStyle}>
                    <div style={cardStyle}>
                        <h2 style={{ marginTop: 0, textAlign: "center" }}>Please sign in</h2>

                        <div style={{ marginBottom: 12 }}>
                            <label style={{ display: "block", marginBottom: 6, fontWeight: 600 }}>Name</label>
                            <input
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                style={{ width: "95%", padding: 10, borderRadius: 8, border: "1px solid #ccc" }}
                            />
                        </div>

                        <div style={{ marginBottom: 16 }}>
                            <label style={{ display: "block", marginBottom: 6, fontWeight: 600 }}>Password</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                style={{ width: "95%", padding: 10, borderRadius: 8, border: "1px solid #ccc" }}
                            />
                        </div>

                        {error && (
                            <div style={{ color: "#d32f2f", marginBottom: 10, textAlign: "center" }}>{error}</div>
                        )}

                        <div style={centerStyle}>
                            <button
                                onClick={() => {
                                    setError("");
                                    if (!name) {
                                        setError("Please enter a name");
                                        return;
                                    }

                                    if (!password) {
                                        setError("Please enter a password");
                                        return;
                                    }

                                    // Fail on bad username or password with same message
                                    if (name !== ALLOWED_NAME || password !== ALLOWED_PASSWORD) {
                                        setError("Invalid login credentials");
                                        return;
                                    }

                                    onLogin();
                                }}
                                style={{ padding: "10px 14px", borderRadius: 6, cursor: "pointer", fontWeight: 600 }}
                            >
                                Continue
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
