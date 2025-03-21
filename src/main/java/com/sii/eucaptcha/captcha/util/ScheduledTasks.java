package com.sii.eucaptcha.captcha.util;

import com.sii.eucaptcha.configuration.users.CaptchaUsers;
import com.sii.eucaptcha.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by: Eddy Hoevenaers
 * Created on: dinsdag 09 april 2024
 */

@Service
@Slf4j
@Profile("!test")
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class ScheduledTasks {

    private final ResourceLoader resourceLoader;
    private final CaptchaUsers captchaUsers;
    private final CaptchaService captchaService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final String usersFileName = "eu_captcha_reusers.txt";

    @Value("${aws.s3.enabled:false}")
    private boolean awsS3Enabled;

    @Value("${aws.s3.bucket.name:}")
    private String bucketName;

    @Value("${aws.s3.log.bucket.name:}")
    private String logBucketName;

    @Value("${aws.s3.region:EU_WEST_1}")
    private String awsRegion;

    public ScheduledTasks(ResourceLoader resourceLoader, CaptchaUsers captchaUsers, CaptchaService captchaService) {
        this.resourceLoader = resourceLoader;
        this.captchaUsers = captchaUsers;
        this.captchaService = captchaService;
    }

    @PostConstruct
    public void readUserFileOnStartUp() {
        log.info("Started download user file");
        if (awsS3Enabled) {
            try {
                downloadObjectBytesFromAws(getS3Client(), bucketName, usersFileName, loadFile("captcha-users.txt").toString());
                captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
            } catch (Exception e) {
                log.error("Failed to download user file from AWS S3: {}", e.getMessage());
                // Fall back to local file if available
                try {
                    captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
                } catch (Exception ex) {
                    log.error("Failed to read local user file: {}", ex.getMessage());
                }
            }
        } else {
            log.info("AWS S3 integration is disabled. Using local user file.");
            try {
                captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
            } catch (Exception e) {
                log.error("Failed to read local user file: {}", e.getMessage());
            }
        }
    }

    // Executed every 10 minutes
    @Scheduled(fixedDelay = 600000)
    public void readUserFile() {
        log.info("Started download user file");
        if (awsS3Enabled) {
            try {
                downloadObjectBytesFromAws(getS3Client(), bucketName, usersFileName, loadFile("captcha-users.txt").toString());
                captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
            } catch (Exception e) {
                log.error("Failed to download user file from AWS S3: {}", e.getMessage());
            }
        } else {
            log.info("AWS S3 integration is disabled. Using local user file.");
            try {
                captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
            } catch (Exception e) {
                log.error("Failed to read local user file: {}", e.getMessage());
            }
        }
    }

    // Executed once a day
    @Scheduled(fixedDelay = 86400000)
    public void uploadCounterFile() {
        try {
            FileUtil.writeCsvFile(loadFile("user-counter.csv").toString(), captchaService.getReportingStatistics());

            if (awsS3Enabled) {
                log.info("Started upload counter file to AWS S3");
                uploadObjectToAws(getS3Client(), bucketName, "/counters/daily/users-counter_" + dateFormat.format(new Date()) + ".csv",
                        loadFile("user-counter.csv").toString());
            } else {
                log.info("AWS S3 integration is disabled. Counter file saved locally only.");
            }
        } catch (Exception e) {
            log.error("Failed to write or upload counter file: {}", e.getMessage());
        }
    }

    //Executed once a day
    @Scheduled(fixedDelay = 86400000)
    public void uploadLogFile() {
        if (awsS3Enabled) {
            log.info("AWS S3 log upload is currently disabled in code.");
            // Uncomment to enable log file upload
            // log.info("Started upload log file");
            // FileUtil.copyFile();
            // uploadObjectToAws(getS3Client(), logBucketName,"eu-application_" + dateFormat.format(new Date()) + ".log",
            //         "src/main/resources/eu-application.log");
        } else {
            log.info("AWS S3 integration is disabled. Log file not uploaded.");
        }
    }

    private S3Client getS3Client() {
        if (!awsS3Enabled) {
            log.warn("Attempting to get S3 client while AWS S3 integration is disabled");
        }
        return S3Client.builder().region(Region.of(awsRegion)).build();
    }

    private static void downloadObjectBytesFromAws(S3Client s3, String bucketName, String keyName, String path) {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(keyName)
                .bucket(bucketName)
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();
        log.info("data {}", data.length);

        // Write the data to a local file.
        File myFile = new File(path);
        OutputStream os = null;
        try {
            os = Files.newOutputStream(myFile.toPath());
            os.write(data);
            log.info("Successfully obtained bytes from an S3 object");
            os.close();
        } catch (IOException exception) {
            log.info(exception.getMessage());
        }
    }

    private static void uploadObjectToAws(S3Client s3, String bucketName, String keyName, String objectPath) {
        try {
            log.info("fileName {} and path {}", keyName, objectPath);

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();
            File fileToUpload = new File(objectPath);
            log.info("IsFile {}, content: {}", fileToUpload.exists(), fileToUpload);
            s3.putObject(putOb, RequestBody.fromFile(fileToUpload));
            log.info("Successfully placed " + keyName + " into bucket " + bucketName);

        } catch (S3Exception e) {
            log.info(e.getMessage());
            System.exit(1);
        }
    }

    private Resource loadFile (String filename) {
        return resourceLoader.getResource(
              "classpath:" + filename);
    }
}
