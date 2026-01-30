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

    // Roster title from XML
    const [rosterTitle, setRosterTitle] = useState("");
    const [rosterStatus, setRosterStatus] = useState<"loading" | "ready" | "error">("loading");



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

        fetch("/roster.xml")
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
                const loadedTitle = titleNode?.textContent?.trim() || "Roster";
                setRosterTitle(loadedTitle);

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

    const displayRosterTitle = rosterTitle && rosterTitle.trim() ? rosterTitle : "Roster";

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setMessage("");

        // If user did nothing
        if (!searchText.trim() && !radioName) {
            setMessage("Please type or select name");
            return;
        }

        // Must select a valid radio option
        let selectedName = radioName;

        // Auto-select if exactly one match
        if (!selectedName && filteredNames.length === 1) {
            selectedName = filteredNames[0];
        }

        if (!selectedName) {
            setMessage("Name Not Found");
            return;
        }

        const timestamp = new Date()
            .toISOString()
            .replace("T", " ")
            .substring(0, 19);

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

    // Shared card style 
    const cardStyle: React.CSSProperties = {
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
        maxWidth: 500,      // match cardStyle.maxWidth
        margin: "0 auto",
        boxSizing: "border-box",
    };



    // Top nav style
    const navStyle: React.CSSProperties = {
        width: "100%",
        backgroundColor: "#2a909a",
        padding: "10px 10px",
        boxSizing: "border-box",
    };

    return (
        <div style={{ width: "100%" }}>
            {/* NAV BAR */}
            <nav style={navStyle}>
                <h1
                    style={{
                        margin: 0,
                        color: "#000",
                        fontSize: "1.8rem",
                        fontWeight: 600,
                        textAlign: "left",
                    }}
                >
                    Attendance Tracking Application
                </h1>
            </nav>



            {/* PAGE CONTENT */}
            <div style={{ marginTop: 30, paddingBottom: 40 }}>
                <div style={contentWrapStyle}>
                    {/* ROSTER TITLE (from XML) */}
                    <div
                        style={{
                            marginBottom: 16,
                            fontWeight: 700,
                            fontSize: "2rem",
                            color: "#222",
                            textAlign: "left",
                            position: "relative",
                            left: -50,
                        }}
                    >
                        {displayRosterTitle}
                    </div>

                    {/* SUCCESS CONFIRMATION SCREEN */}
                    {submitted ? (
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
                    ) : (
                        // INITIAL SUBMISSION SCREEN
                        <form onSubmit={handleSubmit} style={cardStyle}>
                            <label style={{ display: "block", marginBottom: 8, fontSize: "1.4rem" }}>
                                Type Student Name
                            </label>

                            <input
                                ref={searchRef}
                                type="text"
                                placeholder="Type name..."
                                value={searchText}
                                onChange={(e) => {
                                    setSearchText(e.target.value);
                                    setRadioName("");
                                    setMessage("");
                                }}
                                aria-invalid={!!message}
                                aria-describedby={message ? "nameError" : undefined}
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

                            {filteredNames.length > 0 && (
                                <p
                                    style={{
                                        margin: "12px 0",
                                        color: "#666",
                                        fontSize: "0.9rem",
                                        fontWeight: 500,
                                        textTransform: "uppercase",
                                    }}
                                >
                                    OR Select The Name
                                </p>
                            )}

                            <div
                                role="radiogroup"
                                aria-describedby={message ? "nameError" : undefined}
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
                                    filteredNames.map((name) => (
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
                                    ))
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
                    )}
                </div>
            </div>
        </div>
    );
}

export default App;
