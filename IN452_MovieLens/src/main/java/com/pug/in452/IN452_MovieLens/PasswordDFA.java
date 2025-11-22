package com.pug.in452.IN452_MovieLens;

/**
 * PasswordDFA provides a simple deterministic finite automaton (DFA)-style
 * password validator. It inspects a password string and reports
 * - whether it satisfies configured minimum requirements
 * - a textual feedback message describing missing requirements
 * - a strength score (0..4) and a human readable label for that score
 *
 * The validator is intentionally straightforward and counts the presence of
 * character classes (digit, uppercase, lowercase, special) to compute the
 * strength score. The minimum length requirement is treated specially: if the
 * password is shorter than the required minimum, the implementation currently
 * forces the strength score to 0 (considered "Invalid Password Length").
 *
 * This class is configurable via constructors and setters so tests or other
 * code can enable/disable specific requirements and change the minimum length.
 */
public class PasswordDFA {

    /**
     * The set of special characters considered valid for the "special character"
     * requirement. If a character appears in this String it will be counted as
     * a special character by the validator.
     */
    public static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?/[]{}|~";

    // Configuration flags: whether each class is required. These influence both
    // validation feedback and the 'valid' boolean produced by validate().
    private boolean hasDigit;
    private boolean hasUpperCase;
    private boolean hasLowerCase;
    private boolean hasSpecialChar;

    // Minimum required password length. Default constructor sets this to 6.
    private int minLength;

    /**
     * Default constructor.
     * The default configuration requires: digit, uppercase, lowercase, special
     * character and a minimum length of 6 characters. Tests or calling code may
     * override these using the parameterized constructor or individual setters.
     */
    public PasswordDFA() {
        this(true, true, true, true, 6);
    }

    /**
     * Parameterized constructor to create a validator with custom requirements.
     *
     * @param requiresDigit   whether at least one digit (0-9) is required
     * @param requiresUpper   whether at least one uppercase letter (A-Z) is required
     * @param requiresLower   whether at least one lowercase letter (a-z) is required
     * @param requiresSpecial whether at least one special character (from SPECIAL_CHARS) is required
     * @param minimumLength   minimum allowed password length (non-negative)
     */
    public PasswordDFA(boolean requiresDigit, boolean requiresUpper, boolean requiresLower, boolean requiresSpecial,
            int minimumLength) {
        this.hasDigit = requiresDigit;
        this.hasUpperCase = requiresUpper;
        this.hasLowerCase = requiresLower;
        this.hasSpecialChar = requiresSpecial;
        // Ensure minLength is not negative
        this.minLength = Math.max(0, minimumLength);
    }

    /**
     * Validate the provided password string and return a PasswordResult that
     * contains validation boolean, detailed feedback and a strength score.
     *
     * The algorithm performs these steps:
     * 1. Handle null input by treating it as an empty string.
     * 2. Check length against configured minLength.
     * 3. Iterate over each character and record whether it matches one of the
     *    character classes: digit, uppercase, lowercase, or special.
     * 4. Build detailed feedback explaining which requirements are missing.
     * 5. Compute a strength score (0..4) by counting how many character classes
     *    are present. If the length requirement is not met the score is forced
     *    to 0 (treated as an invalid/very weak password).
     *
     * Note: The strength scoring in this implementation is intentionally simple
     * (presence-based). It does not measure entropy or use heuristics such as
     * penalizing dictionary words.
     *
     * @param password the password to validate (may be null)
     * @return a PasswordResult with validation details and a 0..4 strength score
     */
    public PasswordResult validate(String password) {
        if (password == null)
            password = "";

        // Quick checks and per-character predicates
        boolean lengthOk = password.length() >= minLength;
        boolean foundDigit = false;
        boolean foundUpper = false;
        boolean foundLower = false;
        boolean foundSpecial = false;

        // Iterate characters once and record which classes appear
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c))
                foundDigit = true;
            else if (Character.isUpperCase(c))
                foundUpper = true;
            else if (Character.isLowerCase(c))
                foundLower = true;
            else if (SPECIAL_CHARS.indexOf(c) >= 0)
                foundSpecial = true;
            // Characters not matching any of the above are ignored for scoring.
        }

        // Build human-readable feedback about missing requirements
        StringBuilder feedback = new StringBuilder();
        boolean valid = true; // assume valid until a requirement fails

        if (!lengthOk) {
            feedback.append("Password must be at least ").append(minLength).append(" characters long.\n");
            valid = false;
        }
        if (hasDigit && !foundDigit) {
            feedback.append("Password must contain at least one digit (0-9).\n");
            valid = false;
        }
        if (hasUpperCase && !foundUpper) {
            feedback.append("Password must contain at least one uppercase letter (A-Z).\n");
            valid = false;
        }
        if (hasLowerCase && !foundLower) {
            feedback.append("Password must contain at least one lowercase letter (a-z).\n");
            valid = false;
        }
        if (hasSpecialChar && !foundSpecial) {
            feedback.append("Password must contain at least one special character (").append(SPECIAL_CHARS)
                    .append(").\n");
            valid = false;
        }

        if (valid) {
            // If all configured requirements are satisfied, include a short success note
            feedback.append("Password meets the configured requirements.");
        }

        // Compute a simple strength score in range 0..4. Each present character
        // class contributes +1 to the score. This is intentionally simple and is
        // primarily useful for giving immediate, coarse feedback to users.
        int score = 0;
        if (foundDigit)
            score++;
        if (foundUpper)
            score++;
        if (foundLower)
            score++;
        if (foundSpecial)
            score++;

        // If the length requirement is not met we treat the password as very weak
        // and force score to 0. This ensures short passwords like "ABC123" can be
        // considered weak depending on configured minLength.
        if (!lengthOk)
            score = 0;

        // Assemble and return the result
        PasswordResult result = new PasswordResult();
        result.setValid(valid);
        result.setFeedback(feedback.toString());
        result.setStrengthScore(score);
        return result;
    }

    // --- Configuration getters / setters ---

    public boolean isHasDigit() {
        return hasDigit;
    }

    public void setHasDigit(boolean hasDigit) {
        this.hasDigit = hasDigit;
    }

    public boolean isHasUpperCase() {
        return hasUpperCase;
    }

    public void setHasUpperCase(boolean hasUpperCase) {
        this.hasUpperCase = hasUpperCase;
    }

    public boolean isHasLowerCase() {
        return hasLowerCase;
    }

    public void setHasLowerCase(boolean hasLowerCase) {
        this.hasLowerCase = hasLowerCase;
    }

    public boolean isHasSpecialChar() {
        return hasSpecialChar;
    }

    public void setHasSpecialChar(boolean hasSpecialChar) {
        this.hasSpecialChar = hasSpecialChar;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Inner class storing the validation result.
     * Contains: isValid, feedback text, numeric strength score and helper to
     * produce a human-readable strength label.
     */
    public static class PasswordResult {
        private boolean isValid;
        private String feedback;
        private int strengthScore; // 0-4

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public int getStrengthScore() {
            return strengthScore;
        }

        public void setStrengthScore(int strengthScore) {
            this.strengthScore = strengthScore;
        }

        /**
         * Return a short textual label describing the numeric strengthScore.
         * Note: the mapping chosen here is application-specific and may be
         * adjusted as needed.
         */
        public String getStrengthText() {
            switch (strengthScore) {
            case 0:
                return "Invalid Password Length"; // length < minLength forces 0
            case 1:
                return "Very Weak";
            case 2:
                return "Weak";
            case 3:
                return "Strong";
            case 4:
                return "Very Strong";
            default:
                return "Unknown";
            }
        }

        @Override
        public String toString() {
            return "PasswordResult{isValid=" + isValid + ", strengthScore=" + strengthScore + ", feedback='"
                    + feedback + "'}";
        }
    }
}