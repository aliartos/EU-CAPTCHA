package com.sii.eucaptcha.captcha.util;

import com.sii.eucaptcha.configuration.users.CaptchaUsers;
import com.sii.eucaptcha.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
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
import sun.util.logging.resources.logging;

import javax.annotation.PostConstruct;
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
public class ScheduledTasks {

    private ResourceLoader resourceLoader ;
    private CaptchaUsers captchaUsers;
    private CaptchaService captchaService;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    // Replace this with your AWS bucket name for the daily counters
    private String bucketName = "";
    // Replace this with your AWS bucket name for logging
    private String logBucketName = "";
    private String usersFileName = "eu_captcha_reusers.txt";

    public ScheduledTasks(ResourceLoader resourceLoader, CaptchaUsers captchaUsers, CaptchaService captchaService) {
        this.resourceLoader = resourceLoader;
        this.captchaUsers = captchaUsers;
        this.captchaService = captchaService;
    }

    @PostConstruct
    public void readUserFileOnStartUp() {
        log.info("Started download user file");
        downloadObjectBytesFromAws(getS3Client(), bucketName, usersFileName, loadFile("captcha-users.txt").toString());
        captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
    }

    // Executed every 10 minutes
    @Scheduled(fixedDelay = 600000)
    public void readUserFile() {
        log.info("Started download user file");
        downloadObjectBytesFromAws(getS3Client(), bucketName, usersFileName, loadFile("captcha-users.txt").toString());
        captchaUsers.setValidUsers(FileUtil.readFile(loadFile("captcha-users.txt").toString()));
    }

    //Executed once a day
    @Scheduled(fixedDelay = 86400000)
    public void uploadCounterFile() {
        FileUtil.writeFile(loadFile("user-counter.txt").toString(), captchaService.getReportingStatistics());
        log.info("Started upload counter file");
        uploadObjectToAws(getS3Client(), bucketName, "/counters/daily/users-counter_" + dateFormat.format(new Date()) + ".txt"
                , loadFile("user-counter.txt").toString());
    }

    //Executed once a day
    @Scheduled(fixedDelay = 86400000)
    public void uploadLogFile() {
        log.info("Started upload log file");
        FileUtil.copyFile();
        uploadObjectToAws(getS3Client(), logBucketName,"eu-application_" + dateFormat.format(new Date()) + ".log"
                , loadFile("eu-application.log").toString());
    }

    private S3Client getS3Client() {
        return S3Client.builder().region(Region.EU_WEST_1).build();
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
