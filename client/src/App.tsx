import { useEffect, useState } from "react";

function App() {
    const [names, setNames] = useState<string[]>([]);
    const [dropdownName, setDropdownName] = useState("");
    const [radioName, setRadioName] = useState("");

    const [submitted, setSubmitted] = useState(false);
    const [submittedName, setSubmittedName] = useState("");
    const [submittedTime, setSubmittedTime] = useState("");

    const [message, setMessage] = useState("");

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

    function handleDropdownChange(value: string) {
        setDropdownName(value);
        setRadioName("");
    }

    function handleRadioChange(value: string) {
        setRadioName(value);
        setDropdownName("");
    }

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();

        const selectedName = dropdownName || radioName;

        if (!selectedName) {
            setMessage("Please select your name.");
            return;
        }



        // Create timestamp
        const now = new Date();

        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, "0");
        const day = String(now.getDate()).padStart(2, "0");
        const hours = String(now.getHours()).padStart(2, "0");
        const minutes = String(now.getMinutes()).padStart(2, "0");
        const seconds = String(now.getSeconds()).padStart(2, "0");

        const timestamp = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

        // Save submission info
        setSubmittedName(selectedName);
        setSubmittedTime(timestamp);
        setSubmitted(true);
    }

    // SUCCESS PAGE
    if (submitted) {
        return (
            <div style={{ maxWidth: 600, margin: "40px auto", textAlign: "center" }}>
                <h3 style={{ color: "#00c853", marginBottom: "35px" }}>ATTENDANCE LOGGED SUCCESSFULLY!</h3>

                <div
                    style={{
                        border: "1px solid #ccc",
                        borderRadius: "8px",
                        padding: "16px",
                        marginTop: "0px",
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

                <p style={{ marginTop: 30 }}>You may now close this page.</p>
            </div>
        );
    }

    // FORM PAGE
    return (
        <form onSubmit={handleSubmit} style={{ maxWidth: 600, margin: "0 auto" }}>
            <h2>Attendance Tracking System</h2>

            <label>Name (Dropdown)</label>
            <select
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

            <p>{message}</p>
        </form>
    );
}

export default App;
