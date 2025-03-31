package com.sii.eucaptcha.captcha.util;

import com.sii.eucaptcha.captcha.audio.Sample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FileUtil {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * Get a file resource and return it as an InputStream. Intended primarily
     * to read in binary files which are contained in a jar.
     *
     * @param filename the file
     * @return An @{link InputStream} to the file
     */
    public static InputStream readResource(String filename) {
        InputStream jarIs = FileUtil.class.getResourceAsStream(filename);
        if (jarIs == null) {
            throw new RuntimeException(new FileNotFoundException("File '"
                    + filename + "' not found."));
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[16384];
        int nRead;

        try {
            while ((nRead = jarIs.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            jarIs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

    public static Sample readSample(String filename) {
        InputStream is = readResource(filename);
        return new Sample(is);
    }

    public static Map<String, String> readFile(String filePath) {
        Map<String, String> users = new HashMap<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                String[] keyValuePair = line.split(":", 2);
                if (keyValuePair.length > 1) {
                    String key = keyValuePair[0];
                    String value = keyValuePair[1];
                    log.info("Key: {}, value: {}", key, value);
                    users.put(key, value);

                } else {
                    log.info("No Key:Value found in line, ignoring: " + line);
                }
            }
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return users;
    }

    public static void writeFile(String filePath, Map<String, Integer> userCounts) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for(Map.Entry<String, Integer> entry : userCounts.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }

            writer.flush();

        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public static void writeCsvFile(String filePath, Map<String, Integer> userCounts) {

        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(filePath), CSVFormat.DEFAULT)) {
            for(Map.Entry<String, Integer> entry : userCounts.entrySet()) {
                csvPrinter.printRecord(entry.getKey(), entry.getValue(), dateFormat.format(new Date()));
            }
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public static void copyFile() {
        File src = new File("./logs/eu-application.log");
        File dest = new File("src/main/resources/eu-application.log");
        try {
            FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
