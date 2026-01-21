using System;
using System.Windows.Forms;

namespace WindowsFormsApp1
{
    public partial class Form1 : Form
    {
        //Add, remove, or create the name and password here 
        private const string AllowedName = "Ahmad Kassem"; 
        private const string AllowedPassword = "IT488";

        // Optional: limit attempts for login validation
        private int attemptsLeft = 3;

        public Form1()
        {
            InitializeComponent();
            // Set the Accept button to Enter this triggers login
            this.AcceptButton = btnLogin;
        }

        private void btnLogin_Click(object sender, EventArgs e)
        {
            string name = txtName.Text.Trim();
            string password = txtPassword.Text;

            // Simple validation
            if (string.IsNullOrEmpty(name))
            {
                MessageBox.Show("Please enter your name.", "Validation", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                txtName.Focus();
                return;
            }

            if (string.IsNullOrEmpty(password))
            {
                MessageBox.Show("Please enter your password.", "Validation", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                txtPassword.Focus();
                return;
            }

            // This code ensures credentials are valid
            if (name.Equals(AllowedName, StringComparison.Ordinal) &&
                password.Equals(AllowedPassword, StringComparison.Ordinal))
            {
                MessageBox.Show("Login successful.", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);
                // Example action: open main form or close
                // For demo, we'll just close the login form
                this.DialogResult = DialogResult.OK;
                this.Close();
            }
            else
            {
                attemptsLeft--;
                if (attemptsLeft > 0)
                {
                    MessageBox.Show($"Invalid name or password. Attempts left: {attemptsLeft}", "Login Failed", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    txtPassword.Clear();
                    txtPassword.Focus();
                }
                else
                {
                    MessageBox.Show("Too many failed attempts. The application will close.", "Locked Out", MessageBoxButtons.OK, MessageBoxIcon.Stop);
                    Application.Exit();
                }
            }
        }
    }
}
