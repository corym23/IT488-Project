import { useEffect, useState, useRef } from "react";

function App() {
    // Roster names from XML
    const [names, setNames] = useState<string[]>([]);

    // Dropdown OR Radio
    const [dropdownName, setDropdownName] = useState("");
    const [radioName, setRadioName] = useState("");

    // Success screen state
    const [submitted, setSubmitted] = useState(false);
    const [submittedName, setSubmittedName] = useState("");
    const [submittedTime, setSubmittedTime] = useState("");

    // Error message
    const [message, setMessage] = useState("");

    // Log another
    const dropdownRef = useRef<HTMLSelectElement | null>(null);

    // Load roster from XML
    useEffect(() => {
        fetch("/roster.xml")
            .then((response) => response.text())
            .then((xmlText) => {
                const parser = new DOMParser();
                const xml = parser.parseFromString(xmlText, "application/xml");

                const nameElements = xml.getElementsByTagName("name");
                const loadedNames: string[] = [];

                for (let i = 0; i < nameElements.length; i++) {
                    const text = nameElements[i].textContent;
                    if (text) {
                        loadedNames.push(text);
                    }
                }

                setNames(loadedNames);
            })
            .catch(() => {
                setMessage("Error loading roster");
            });
    }, []);

    useEffect(() => {
        if (!submitted) {
            setTimeout(() => {
                dropdownRef.current?.focus();
            }, 0);
        }
    }, [submitted]);

    // Dropdown change = clear radio so they don't work in tandem
    function handleDropdownChange(value: string) {
        setDropdownName(value);
        if (value) {
            setRadioName("");
        }
    }

    // Radio change = clear dropdown so they don't work in tandem
    function handleRadioChange(value: string) {
        setRadioName(value);
        if (value) {
            setDropdownName("");
        }
    }

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setMessage("");

        // dDropdown OR radio
        const selectedName = dropdownName || radioName;

        if (!selectedName) {
            setMessage("Please select your name.");
            return;
        }

        // Timestamp
        const timestamp = new Date()
            .toISOString()
            .replace("T", " ")
            .substring(0, 19);

        // Save submission, then switch to success screen
        setSubmittedName(selectedName);
        setSubmittedTime(timestamp);
        setSubmitted(true);
    }

    // Button on success screen
    function handleLogAnother() {
        // Reset everything back to the beginning
        setSubmitted(false);
        setSubmittedName("");
        setSubmittedTime("");
        setDropdownName("");
        setRadioName("");
        setMessage("");
    }

    // SUCCESS CONFIRMATION SCREEN
    if (submitted) {
        return (
            <div style={{ maxWidth: 600, margin: "30px auto", textAlign: "center" }}>
                <h2 style={{ color: "#00c853", marginBottom: "35px" }}>
                    Attendance Logged Successfully
                </h2>

                <div
                    style={{
<<<<<<< HEAD
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
                        placeholder="First Name Last Name"
                        style={{
                            width: "90%",
                            padding: 12, // increased padding for better touch targets 
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
=======
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
>>>>>>> c1bdd8717166119c7a35b1634852e81966ca12ab
                    </div>

                    <div>
                        <strong>Time Submitted:</strong>
                        <div>{submittedTime}</div>
                    </div>
                </div>

                {/* Log another button */}
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

    // ✅ INITIAL SUBMISSION SCREEN
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
                {names.map((name) => (
                    <option key={name} value={name}>
                        {name}
                    </option>
                ))}
            </select>

            <p style={{ margin: "12px 0" }}>OR</p>

            {names.map((name) => (
                <div key={name}>
                    <label>
                        <input
                            type="radio"
                            name="roster"
                            checked={radioName === name}
                            onChange={() => handleRadioChange(name)}
                        />
                        {name}
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
