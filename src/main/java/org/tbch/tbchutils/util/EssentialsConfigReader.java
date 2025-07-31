package org.tbch.tbchutils.util;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class EssentialsConfigReader {

    public static Map<String, Object> getUserTimestamps(String username) {
        try {
            File file = new File("plugins/Essentials/userdata/" + username.toLowerCase() + ".yml");
            if (!file.exists()) {return new HashMap<>();}

            Yaml yaml = new Yaml();

            try (InputStream input = Files.newInputStream(file.toPath())) {
                Object rawData = yaml.load(input);
                if (!(rawData instanceof Map)) {return null;}

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) rawData;
                Object rawTimestamps = data.get("timestamps");

                if (!(rawTimestamps instanceof Map)) {return null;}

                @SuppressWarnings("unchecked")
                Map<String, Object> timestamps = (Map<String, Object>) rawTimestamps;
                return timestamps;
            } catch (Exception strE) {
                strE.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
