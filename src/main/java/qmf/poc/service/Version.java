package qmf.poc.service;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {
    public static void printVersion(Logger log) {
        String version = getVersion(log);
        System.out.println("Version: " + version);
    }

    static String getVersion(Logger log) {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                return "0.0.0-no-version";
            }

            Properties properties = new Properties();
            properties.load(input);

            return properties.getProperty("version");
        } catch (IOException ex) {
            log.error("Error loading version properties", ex);
            return "0.0.0-no-version";
        }
    }
}
