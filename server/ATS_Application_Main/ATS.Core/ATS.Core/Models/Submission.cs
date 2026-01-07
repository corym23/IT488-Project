using System;

namespace ATS.Core.Models
{
    public class Submission
    {
        public int SubmissionID { get; set; }   // primary key (future sql)
        public int StudentID { get; set; }  // FK to Student
        public DateTime SubmissionTime { get; set; }

        // set by server-side validation logic (API) 
        public bool Success { get; set; } 
    }
}
