import { useEffect, useState, useRef } from "react";

function App() {
    const [names, setNames] = useState<string[]>([]);
    const [dropdownName, setDropdownName] = useState("");
    const [radioName, setRadioName] = useState("");
    const [submitted, setSubmitted] = useState(false);
    const [submittedName, setSubmittedName] = useState("");
    const [submittedTime, setSubmittedTime] = useState("");
    const [message, setMessage] = useState("");
    const dropdownRef = useRef<HTMLSelectElement | null>(null);

    useEffect(() => {
        fetch("/roster.xml")
            .then((r) => r.text())
            .then((xmlText) => {
                const parser = new DOMParser();
                const xml = parser.parseFromString(xmlText, "application/xml");
                const nameElements = xml.getElementsByTagName("name");
                const loaded: string[] = [];
                for (let i = 0; i < nameElements.length; i++) {
                    const t = nameElements[i].textContent;
                    if (t) loaded.push(t);
                }
                setNames(loaded);
            })
            .catch(() => setMessage("Error loading roster"));
    }, []);

    useEffect(() => {
        if (!submitted) dropdownRef.current?.focus();
    }, [submitted]);

    function handleDropdownChange(value: string) {
        setDropdownName(value);
        if (value) setRadioName("");
    }

    function handleRadioChange(value: string) {
        setRadioName(value);
        if (value) setDropdownName("");
    }

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setMessage("");
        const selected = dropdownName || radioName;
        if (!selected) {
            setMessage("Please select your name.");
            return;
        }
        const timestamp = new Date().toISOString().replace("T", " ").substring(0, 19);
        setSubmittedName(selected);
        setSubmittedTime(timestamp);
        setSubmitted(true);
    }

    function handleLogAnother() {
        setSubmitted(false);
        setSubmittedName("");
        setSubmittedTime("");
        setDropdownName("");
        setRadioName("");
        setMessage("");
    }

    if (submitted) {
        return (
            <div style={{ maxWidth: 600, margin: "30px auto", textAlign: "center" }}>
                <h2 style={{ color: "#00c853", marginBottom: "35px" }}>Attendance Logged Successfully</h2>

                <div
                    style={{
                        border: "1px solid #ccc",
                        borderRadius: "8px",
                        padding: "16px",
                        backgroundColor: "#f9f9f9",
                        textAlign: "center",
                    }}
                >
                    <div style={{ marginBottom: "10px" }}>
                        <strong>Student Name:</strong>
                        <div>{submittedName}</div>
                    </div>

                    <div>
                        <strong>Time Submitted:</strong>
                        <div>{submittedTime}</div>
                    </div>
                </div>

                <button
                    type="button"
                    onClick={handleLogAnother}
                    style={{
                        marginTop: 20,
                        padding: "10px 14px",
                        borderRadius: 5,
                        border: "1px solid #222",
                        backgroundColor: "#f9f9f9",
                        cursor: "pointer",
                        fontWeight: 600,
                    }}
                >
                    Log Another
                </button>
            </div>
        );
    }

    return (
        <form onSubmit={handleSubmit} style={{ maxWidth: 600, margin: "0 auto" }}>
            <h2>Attendance Form</h2>

            <label>Name (Dropdown)</label>
            <select
                ref={dropdownRef}
                value={dropdownName}
                onChange={(e) => handleDropdownChange(e.target.value)}
                style={{ width: "100%", padding: 8 }}
            >
                <option value="">Select your name</option>
                {names.map((n) => (
                    <option key={n} value={n}>
                        {n}
                    </option>
                ))}
            </select>

            <p style={{ margin: "12px 0" }}>OR</p>

            {names.map((n) => (
                <div key={n}>
                    <label>
                        <input type="radio" name="roster" checked={radioName === n} onChange={() => handleRadioChange(n)} />
                        {n}
                    </label>
                </div>
            ))}

            <button type="submit" style={{ marginTop: 15 }}>
                Submit
            </button>

            <p style={{ color: "crimson" }}>{message}</p>
        </form>
    );
}

export default App;

