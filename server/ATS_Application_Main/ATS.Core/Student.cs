using System;
using System.Xml.Serialization;





namespace ATS.Core.Models
{
    [XmlRoot("Student")]
    public class Student
    {
        [XmlElement("ID")]
        public int ID { get; set; }

        [XmlElement("FirstName")]
        public string FirstName { get; set; } = string.Empty;

        [XmlElement("LastName")]
        public string LastName { get; set; } = string.Empty;

        [XmlElement("ClassID")]
        public int ClassID { get; set; }

        [XmlElement("ClassName")]
        public string ClassName { get; set; } = string.Empty;
    }
}

