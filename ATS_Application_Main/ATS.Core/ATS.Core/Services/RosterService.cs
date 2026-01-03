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
    }
}






