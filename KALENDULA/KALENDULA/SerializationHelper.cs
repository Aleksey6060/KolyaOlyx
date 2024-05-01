using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;

namespace KALENDAR
{
    public static class SerializationHelper
    {
        public static void SerializeToJson<T>(string filePath, T data)
        {
            string jsonString = JsonConvert.SerializeObject(data, Formatting.Indented);
            File.WriteAllText(filePath, jsonString);
        }

        public static T DeserializeFromJson<T>(string filePath)
        {
            string jsonString = File.ReadAllText(filePath);
            return JsonConvert.DeserializeObject<T>(jsonString);
        }
    }
}