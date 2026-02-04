const express = require("express");
const cors = require("cors");
const bodyParser = require("body-parser");

const app = express();
app.use(cors());
app.use(bodyParser.json());

// Demo credentials (mirror desktop app)
const AllowedName = "Ahmad Kassem";
const AllowedPassword = "IT488";

app.post("/api/login", (req, res) => {
  const { name, password } = req.body || {};
  if (!name || !password) {
    return res
      .status(400)
      .json({ ok: false, message: "Missing name or password" });
  }

  if (name === AllowedName && password === AllowedPassword) {
    // In a real app return a token
    return res.json({ ok: true, message: "Login successful" });
  }

  return res.status(401).json({ ok: false, message: "Invalid credentials" });
});

const port = process.env.PORT || 4000;
app.listen(port, () => console.log(`Web API listening on ${port}`));
