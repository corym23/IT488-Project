import { useState } from "react";

const ROSTER = ["Adam Voss", "Cory Mccombs", "Richard Sanchez", "Jacqueline Vo"];

export default function App() {
    const [typedName, setTypedName] = useState("");
    const [selectedName, setSelectedName] = useState("");
    const [message, setMessage] = useState<string>("");

    function validateTypedName(name: string) {
        const normalized = name.trim().toLowerCase();
        return ROSTER.some((n) => n.toLowerCase() === normalized);
    }

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setMessage("");

        if (!validateTypedName(typedName)) {
            setMessage("Name not found in roster. Please check spelling.");
            return;
        }

        if (!selectedName) {
            setMessage("Please select your name from the roster.");
            return;
        }

        if (typedName.trim().toLowerCase() !== selectedName.toLowerCase()) {
            setMessage("Typed name and selected name must match.");
            return;
        }

        setMessage("Submission successful!");
    }

    return (
        <div style={{
            minHeight: "100vh",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            fontFamily: "system-ui",
            padding: 24,
        }}>

            <div
                style={{
                    width: "100%",
                    maxWidth: 850, 
                }}
            >

                <h1 style={{ margin: "0 0 12px 0" }}>Attendance Tracking System</h1>
                <p style={{ margin: "0 0 16px 0", color: "#444" }}>
                    Type your name, select it from the roster, then submit.
                </p>

                <form
                    onSubmit={handleSubmit}
                    style={{
                        border: "1px solid #ddd",
                        padding: 20,
                        borderRadius: 12,
                        background: "#fff",
                    }}>
                    <label style={{ display: "block", fontWeight: 600, marginBottom: 8 }}>
                        Type your name
                    </label>
                    <input
                        value={typedName}
                        onChange={(e) => setTypedName(e.target.value)}
                        placeholder="Last First Name"
                        style={{
                            width: "100%",
                            padding: 12,
                            borderRadius: 10,
                            border: "1px solid #ccc",
                        }} />

                    <div style={{ marginTop: 18 }}>
                        <div style={{ fontWeight: 600, marginBottom: 10 }}>Select your name</div>

                        {ROSTER.map((name) => (
                            <label key={name} style={{ display: "flex", gap: 10, alignItems: "center", padding: 6 }}>
                                <input
                                    type="radio"
                                    name="roster"
                                    checked={selectedName === name}
                                    onChange={() => setSelectedName(name)}
                                />
                                {name}
                            </label>
                        ))}
                    </div>

                    <button
                        type="submit Attendance"
                        style={{
                            marginTop: 18,
                            padding: "10px 14px",
                            borderRadius: 10,
                            border: "1px solid #222",
                            background: "#fff",
                            fontWeight: 600,
                        }}>
                        Submit
                    </button>

                    <div style={{ marginTop: 14, minHeight: 24 }}>
                        <p style={{ margin: 0 }}>{message}</p>
                    </div>
                </form>
            </div>
        </div>
    );
}
