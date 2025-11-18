package com.pug.in452.IN452_MovieLens;

/**
 * PasswordDFA class to validate passwords based on configurable rules using a
 * DFA approach.
 */

public class PasswordDFA {

	public static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?/[]{}|~";

	private boolean hasDigit;
	private boolean hasUpperCase;
	private boolean hasLowerCase;
	private boolean hasSpecialChar;
	private int minLength;

	// Default constructor: require digits, upper, lower, special, minimum length 6
	public PasswordDFA() {
		this(true, true, true, true, 6);
	}

	// Parameterized constructor to set requirements
	public PasswordDFA(boolean requiresDigit, boolean requiresUpper, boolean requiresLower, boolean requiresSpecial,
			int minimumLength) {
		this.hasDigit = requiresDigit;
		this.hasUpperCase = requiresUpper;
		this.hasLowerCase = requiresLower;
		this.hasSpecialChar = requiresSpecial;
		this.minLength = Math.max(0, minimumLength);
	}

	// Validate password and return result
	public PasswordResult validate(String password) {
		if (password == null)
			password = "";
		boolean lengthOk = password.length() >= minLength;
		boolean foundDigit = false;
		boolean foundUpper = false;
		boolean foundLower = false;
		boolean foundSpecial = false;
		// Check each character in the password
		for (char c : password.toCharArray()) {
			if (Character.isDigit(c))
				foundDigit = true;
			else if (Character.isUpperCase(c))
				foundUpper = true;
			else if (Character.isLowerCase(c))
				foundLower = true;
			else if (SPECIAL_CHARS.indexOf(c) >= 0)
				foundSpecial = true;
		}

		// Build feedback message
		StringBuilder feedback = new StringBuilder();
		boolean valid = true;

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
			feedback.append("Password meets the configured requirements.");
		}

		// Strength score 0-4 based on presence of character classes (digit, upper,
		// lower, special)
		int score = 0;

		// Length requirement met adds to score
		if (foundDigit)
			score++;
		if (foundUpper)
			score++;
		if (foundLower)
			score++;
		if (foundSpecial)
			score++;
		if (!lengthOk)
			score = 0;


		PasswordResult result = new PasswordResult();
		result.setValid(valid);
		result.setFeedback(feedback.toString());
		result.setStrengthScore(score);
		return result;
	}

	// Getters / setters for configuration
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

	// Inner class to hold validation result
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

		// Get textual representation of strength score
		public String getStrengthText() {
			switch (strengthScore) {
			case 0:
				return "Invalid Password Length";
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

		// Override toString for easy debugging
		@Override
		public String toString() {
			return "PasswordResult{isValid=" + isValid + ", strengthScore=" + strengthScore + ", feedback='" + feedback
					+ "'}";
		}
	}
}