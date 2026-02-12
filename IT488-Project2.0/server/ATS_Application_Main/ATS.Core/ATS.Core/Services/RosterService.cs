using System.Collections.Generic;
using System.IO;
using System.Xml.Serialization;
using ATS.Core.Models;

namespace ATS.Core.Services
{

    // service responsible for loading the roster from XML
    // acts as a temporary data source until SQL is implemented
    public class RosterService
    {
        public List<Student> LoadRoster(string xmlFilePath)
        {
            var serializer = new XmlSerializer(
                typeof(List<Student>),
                new XmlRootAttribute("Roster")
             );

            using var stream = new FileStream(xmlFilePath, FileMode.Open);
            return (List<Student>)serializer.Deserialize(stream)!;


        }
        // New method for ATS-62: get preset student names for autocomplete/suggestions
        public List<string> GetPresetStudentNames()
        {
            var names = new List<string>();

            // Load the roster XML
            List<Student> roster;
            try
            {
                roster = LoadRoster("ATS_Application_Main/ATS.Core/Data/XMLFile1.xml");
            }
            catch
            {
                // If XML fails, return a small fallback list
                roster = new List<Student>
                {
                    new Student { FirstName = "Adam", LastName = "Voss" },
                    new Student { FirstName = "Jacqueline", LastName = "Vo" }
                };
            }

            // Convert each Student object into a "Full Name" string
            foreach (var student in roster)
            {
                names.Add($"{student.FirstName} {student.LastName}");
            }

            return names; // return the list of full names
        }

        


    }
}






