import { useEffect, useRef, useState, type FormEvent, type CSSProperties } from "react";
import Logon from "./Logon";


function App() {
    // Roster names from XML
    const [names, setNames] = useState<string[]>([]);

    // Search text for filtering names
    const [searchText, setSearchText] = useState("");

    // Radio selection
    const [selectedNames, setSelectedNames] = useState<string[]>([]);


    // Success screen state
    const [submitted, setSubmitted] = useState(false);
    const [submittedTime, setSubmittedTime] = useState("");

    const [shouldFocusSearch, setShouldFocusSearch] = useState(false);
    const [loggedIn, setLoggedIn] = useState(false);

    // Roster title from XML
    const [rosterTitle, setRosterTitle] = useState("");
    const [, setRosterStatus] = useState<"loading" | "ready" | "error">("loading");

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

                // Class time
                const timeNode = xmlDoc.getElementsByTagName("classTime")[0];
                const loadedTime = timeNode?.textContent?.trim() || "";
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


    useEffect(() => {
        if (shouldFocusSearch) {
            setTimeout(() => {
                searchRef.current?.focus();
            }, 0);
            setShouldFocusSearch(false);
        }
    }, [shouldFocusSearch]);

    function toggleName(name: string) {
        setSelectedNames((prev) => {
            if (prev.includes(name)) {
                return prev.filter((n) => n !== name); // uncheck
            }
            return [...prev, name]; // check
        });
    }


    const filteredNames = names.filter((name) =>
        name.toLowerCase().includes(searchText.toLowerCase())
    );

    const total = names.length;
    const count = selectedNames.length;
    const percent = total > 0 ? Math.round((count / total) * 100) : 0;


    const displayRosterTitle = rosterTitle && rosterTitle.trim() ? rosterTitle : "Roster";

    function handleSubmit(event: FormEvent) {
        event.preventDefault();
        setMessage("");

        // If user did nothing
        if (!searchText.trim() && selectedNames.length === 0) {
            setMessage("Please type or select name");
            return;
        }

        // If they typed but didn't select anything
        if (selectedNames.length === 0) {
            setMessage("Name Not Found");
            return;
        }

        const timestamp = new Date().toISOString().replace("T", " ").substring(0, 19);

        // Save submission
        setSubmittedTime(timestamp);
        setSubmitted(true);
    }



    function handleLogAnother() {
        setSubmitted(false);
        setSubmittedTime("");
        setSearchText("");
        setMessage("");
        setShouldFocusSearch(true);
        setSelectedNames([]);
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
    };


    if (!loggedIn) {
        return <Logon onLogin={() => setLoggedIn(true)} />;
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


                    {/* SUCCESS CONFIRMATION SCREEN */}
                    {submitted ? (
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

                            {/* Percentage + time */}
                            <div style={{ textAlign: "center", marginBottom: 12, fontSize: "1.05rem" }}>
                                <div style={{ marginBottom: 6 }}>
                                    <strong>Percentage:</strong>{" "}
                                    {selectedNames.length} / {names.length} ({percent}%)
                                </div>

                                <div style={{ marginBottom: 14 }}>
                                    <strong>Time Submitted:</strong> {submittedTime}
                                </div>

                                {/* Log another NOW lives here */}
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

                            {/* Selected students box */}
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
                                    <strong style={{ fontSize: "1.05rem" }}>
                                        Selected Students:
                                    </strong>
                                </div>

                                {selectedNames.map((n) => (
                                    <div key={n} style={{ marginBottom: 6 }}>
                                        {n}
                                    </div>
                                ))}
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
                                                            console.log("Uploaded roster:", reader.result);
                                                        };
                                                        reader.readAsText(file);
                                                    }}
                                                />
                                            </label>

                                            {/* Separator */}
                                            <span style={{ color: "#999" }}>|</span>

                                            {/* Download */}
                                            <button
                                                type="button"
                                                onClick={() => {
                                                    window.location.href = "/roster.xml";
                                                }}
                                                style={{
                                                    background: "none",
                                                    border: "none",
                                                    padding: 0,
                                                    color: "#2a909a",
                                                    cursor: "pointer",
                                                    fontSize: "0.7rem",
                                                }}
                                            >
                                                Download
                                            </button>
                                        </div>

                                    </div>
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
                                                    type="checkbox"
                                                    checked={selectedNames.includes(name)}
                                                    onChange={() => toggleName(name)}
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
                                </form>
                    )}
                </div>
            </div>
        </div>
    );
}

export default App;
