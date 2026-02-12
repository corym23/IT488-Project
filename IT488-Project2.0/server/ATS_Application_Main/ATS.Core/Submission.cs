using System;

namespace ATS.Core
{
    public class Submission
    {
        public int SubmissionID { get; set; }
        public int StudentID { get; set; }
        public DateTime SubmissionTime { get; set; }
        public bool Success { get; set; } // true = valid, false = invalid
    }
}
