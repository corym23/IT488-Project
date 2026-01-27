import { useEffect, useRef, useState } from "react";

function App() {
    // Roster names from XML
    const [names, setNames] = useState<string[]>([]);

    // Search text for filtering names
    const [searchText, setSearchText] = useState("");

    // Radio selection
    const [radioName, setRadioName] = useState("");

    // Success screen state
    const [submitted, setSubmitted] = useState(false);
    const [submittedName, setSubmittedName] = useState("");
    const [submittedTime, setSubmittedTime] = useState("");

    const [shouldFocusSearch, setShouldFocusSearch] = useState(false);

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
        fetch("/roster.xml")
            .then((response) => response.text())
            .then((xmlText) => {
                const parser = new DOMParser();
                const xml = parser.parseFromString(xmlText, "application/xml");

                const nameElements = xml.getElementsByTagName("name");
                const loadedNames: string[] = [];

                for (let i = 0; i < nameElements.length; i++) {
                    const text = nameElements[i].textContent;
                    if (text && text.trim()) {
                        loadedNames.push(text.trim());
                    }
                }

                setNames(loadedNames);
            })
            .catch(() => {
                setMessage("Error loading roster");
            });
    }, []);


    useEffect(() => {
        if (shouldFocusSearch) {
            setTimeout(() => {
                searchRef.current?.focus();
            }, 0);
            setShouldFocusSearch(false);
        }
    }, [shouldFocusSearch]);

    function handleRadioChange(value: string) {
        setRadioName(value);
    }

    const filteredNames = names.filter((name) =>
        name.toLowerCase().includes(searchText.toLowerCase())
    );

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setMessage("");

        // Must select a valid radio option
        let selectedName = radioName;

        if (!selectedName && filteredNames.length === 1) {
            selectedName = filteredNames[0];
        }

        if (!selectedName) {
            setMessage("Name Not Found");
            return;
        }


        const timestamp = new Date().toISOString().replace("T", " ").substring(0, 19);

        setSubmittedName(selectedName);
        setSubmittedTime(timestamp);
        setSubmitted(true);
    }

    function handleLogAnother() {
        setSubmitted(false);
        setSubmittedName("");
        setSubmittedTime("");
        setSearchText("");
        setRadioName("");
        setMessage("");
        setShouldFocusSearch(true);
    }

    // Shared card style (keeps widths consistent)
    const cardStyle: React.CSSProperties = {
        width: "100%",
        maxWidth: 480,
        margin: "0 auto",
        padding: 20,
        border: "1px solid #ccc",
        borderRadius: 8,
        boxSizing: "border-box",
    };

    // SUCCESS CONFIRMATION SCREEN
    if (submitted) {
        return (
            <div style={cardStyle}>
                <h2
                    style={{
                        color: "#00c853",
                        marginBottom: 35,
                        textAlign: "center",
                        fontSize: "1.8rem",
                    }}
                >
                    Attendance Logged Successfully
                </h2>

                <div
                    style={{
                        border: "1px solid #ccc",
                        borderRadius: 10,
                        padding: 22,
                        backgroundColor: "#f9f9f9",
                        textAlign: "center",
                        fontSize: "1.1rem",
                    }}
                >
                    <div style={{ marginBottom: 10 }}>
                        <strong style={{ fontSize: "1.05rem" }}>Student Name:</strong>
                        <div>{submittedName}</div>
                    </div>

                    <div>
                        <strong style={{ fontSize: "1.05rem" }}>Time Submitted:</strong>
                        <div>{submittedTime}</div>
                    </div>
                </div>

                <div style={{ textAlign: "center", marginTop: 20 }}>
                    <button
                        type="button"
                        onClick={handleLogAnother}
                        style={{
                            padding: "10px 14px",
                            borderRadius: 5,
                            border: "1px solid #222",
                            backgroundColor: "#f9f9f9",
                            cursor: "pointer",
                            fontWeight: 600,
                        }}
                    >
                        Log another
                    </button>
                </div>
            </div>
        );
    }

    // INITIAL SUBMISSION SCREEN
    return (
        <form onSubmit={handleSubmit} style={cardStyle}>
            <h2 style={{ textAlign: "center" }}>Attendance Form</h2>

            <label>Search Name</label>
            <input
                ref={searchRef}
                type="text"
                placeholder="Type your name..."
                value={searchText}
                onChange={(e) => {
                    setSearchText(e.target.value);
                    setRadioName("");
                    setMessage("");
                }}
                aria-invalid={!!message}
                aria-describedby={message ? "nameError" : undefined}
                style={{ width: "100%", padding: 8, boxSizing: "border-box" }}
            />

            {filteredNames.length > 0 && (
                <p
                    style={{
                        margin: "16px 0",
                        color: "#666",
                        fontSize: "0.9rem",
                        fontWeight: 500,
                        textTransform: "uppercase",
                    }}
                >
                    OR
                </p>
            )}

            <div role="radiogroup" aria-describedby={message ? "nameError" : undefined} aria-invalid={!!message}>
                {filteredNames.map((name) => (
                    <div key={name} style={{ marginBottom: 8 }}>
                        <label>
                            <input
                                type="radio"
                                name="roster"
                                checked={radioName === name}
                                onChange={() => handleRadioChange(name)}
                                style={{ marginRight: 8 }}
                            />
                            {name}
                        </label>
                    </div>
                ))}
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

            {message && (
                <p
                    id="nameError"
                    role="alert"
                    style={{ color: "crimson", textAlign: "center", marginTop: 12 }}
                >
                    {message}
                </p>
            )}

        </form>
    );
}

export default App;
