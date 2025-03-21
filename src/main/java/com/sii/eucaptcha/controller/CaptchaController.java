package com.sii.eucaptcha.controller;

import com.google.gson.JsonObject;
import com.sii.eucaptcha.configuration.users.CaptchaUsers;
import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import com.sii.eucaptcha.controller.dto.captchaquery.CaptchaQueryDto;
import com.sii.eucaptcha.controller.dto.captcharesult.CaptchaResultDto;
import com.sii.eucaptcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


/**
 * @author mousab.aidoud
 * Captcha Rest Controller class with those methodes : getCaptchaImage , reloadCaptchaImage , validateCaptcha.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class CaptchaController {

    private final CaptchaService captchaService;
    private final CaptchaUsers captchaUsers;

    @Value("${aws.enabled:false}")
    private boolean awsEnabled;

    public CaptchaController(CaptchaService captchaService, CaptchaUsers captchaUsers) {
        this.captchaService = captchaService;
        this.captchaUsers = captchaUsers;
    }


    /**
     * Get the Captcha (Id + Captcha Image + Captcha Audio)
     *
     * @param locale the chosen locale
     * @return response as String contains CaptchaID and Captcha Image
     */
    @CrossOrigin
    @Operation(summary = "Get a Captcha image",
            description = "Returns a captcha image as per locale, captchaLength, type and capitalization or not")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved captcha image"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable - Locale is missing or invalid"),
            @ApiResponse(responseCode = "400", description = "Token is missing")
    })
    @GetMapping(value = "/captchaImg")
    public CaptchaResultDto getCaptchaImage(@RequestParam(defaultValue = "en-GB", required = false) String locale,
                                            @RequestParam(defaultValue = "8", required = false) Integer captchaLength,
                                            @RequestParam(defaultValue = CaptchaConstants.STANDARD, required = false) String captchaType,
                                            @RequestParam(defaultValue = "true", required = false) boolean capitalized,
                                            @RequestParam(required = false) Integer degree,
                                            @RequestHeader(required = false) String xJwtString) {

        log.debug("Request with token: {}, language: {}, length: {}, type: {}, capitalized: {} and degrees: {}",
                xJwtString, locale, captchaLength, captchaType, capitalized, degree);

        if (StringUtils.isBlank(locale) || StringUtils.equalsIgnoreCase("Undefined", locale)) {
            log.debug("Locale is missing or invalid!");
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Locale is missing or invalid!");
        }
        if (StringUtils.isBlank(xJwtString) || (awsEnabled && captchaUsers.isNoValidUser(xJwtString))) {
            log.debug("Token is missing or invalid! AWS enabled: {}", awsEnabled);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is missing or invalid!");
        }

        CaptchaQueryDto captchaQueryDto = new CaptchaQueryDto.CaptchaQueryDtoBuilder(captchaType)
                .captchaLength(captchaLength)
                .locale(locale)
                .degree(degree)
                .capitalized(capitalized)
                .xJwtString(xJwtString)
                .build();

        return captchaService.generateCaptchaWrapper(captchaQueryDto);
    }

    /**
     * Reloading the captcha Image
     *
     * @param previousCaptchaId the ID of the previous Captcha
     * @param locale            the chosen Locale
     * @return response as String contains CaptchaID and Captcha Image
     */
    @CrossOrigin
    @Operation(summary = "Refresh a previous Captcha image",
            description = "Returns a new captcha image as per locale, captchaLength, type and capitalization or not")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved captcha image"),
            @ApiResponse(responseCode = "400", description = "CaptchaId is missing"),
            @ApiResponse(responseCode = "400", description = "Token is missing"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable - Locale is missing or invalid")
    })
    @GetMapping(value = "/reloadCaptchaImg/{previousCaptchaId}")
    public CaptchaResultDto reloadCaptchaImage(@PathVariable("previousCaptchaId") String previousCaptchaId,
                                               @RequestParam(required = false) String locale,
                                               @RequestParam(required = false) Integer captchaLength,
                                               @RequestParam(defaultValue = CaptchaConstants.STANDARD, required = false) String captchaType,
                                               @RequestParam(required = false) boolean capitalized,
                                               @RequestParam(required = false) Integer degree,
                                               @RequestHeader(required = false) String xJwtString) {

        log.debug("Reload requested with token: {}, previousCaptchaId: {}, language: {}, length: {}, type: {}, capitalized: {} and degrees: {}",
                xJwtString, previousCaptchaId, locale, captchaLength, captchaType, capitalized, degree);

        if (StringUtils.isBlank(locale) || StringUtils.equalsIgnoreCase("Undefined", locale)) {
            log.debug("Locale is missing or invalid!");
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Locale is missing or invalid!");
        }
        if (StringUtils.isBlank(xJwtString) || (awsEnabled && captchaUsers.isNoValidUser(xJwtString))) {
            log.debug("Token is missing or invalid! AWS enabled: {}", awsEnabled);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is missing or invalid!");
        }

        if (StringUtils.isBlank(previousCaptchaId)) {
            log.debug("CaptchaId is missing!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CaptchaId is missing!");
        }


        CaptchaQueryDto captchaQueryDto = new CaptchaQueryDto.CaptchaQueryDtoBuilder(captchaType)
                .captchaLength(captchaLength)
                .previousCaptchaId(previousCaptchaId)
                .locale(locale)
                .degree(degree)
                .capitalized(capitalized)
                .xJwtString(xJwtString)
                .build();
        return captchaService.generateCaptchaWrapper(captchaQueryDto);
    }

    /**
     * Validating the captcha answer :
     *
     * @param captchaId     the ID of the Captcha
     * @param captchaAnswer the answer of the Captcha -> success or fail
     * @return fail or success as String response
     */
    @CrossOrigin
    @Operation(summary = "Validate a Captcha image",
            description = "Returns success or failed as an answer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfull response can be success or fail"),
            @ApiResponse(responseCode = "400", description = "CaptchaId is missing"),
            @ApiResponse(responseCode = "400", description = "Token is missing")
    })
    @PostMapping(value = "/validateCaptcha/{captchaId}")
    public ResponseEntity<String> validateCaptcha(@PathVariable(value = "captchaId", required = false) String captchaId,
                                                  @RequestParam(value = "captchaAnswer", required = false) String captchaAnswer,
                                                  @RequestParam(value = "useAudio", required = false) boolean useAudio,
                                                  @RequestParam(value = "captchaType", defaultValue = CaptchaConstants.STANDARD, required = false) String captchaType,
                                                  @RequestHeader(required = false) String xJwtString) {

        log.debug("Validation requested with token: {}, captchaId: {}, captchaAnswer: {}, useAudio: {}, type: {}",
                xJwtString, captchaId, captchaAnswer, useAudio, captchaType);

        //check if captchaId is present
        if (StringUtils.isBlank(captchaId)) {
            log.error("CaptchaId is missing!");
            return new ResponseEntity<>("CaptchaId is missing!", HttpStatus.BAD_REQUEST);
        } else if (StringUtils.isBlank(xJwtString) || (awsEnabled && captchaUsers.isNoValidUser(xJwtString))) {
            log.error("Token is missing or invalid! AWS enabled: {}", awsEnabled);
            return new ResponseEntity<>("Token is missing or invalid!", HttpStatus.BAD_REQUEST);
        } else {
            //Verify the validity of the captcha answer.
            try {
                boolean responseCaptcha;
                responseCaptcha = captchaService.validateCaptcha(xJwtString, captchaId, captchaAnswer, captchaType, useAudio);
                JsonObject response = new JsonObject();
                //response captcha ( valid -> success || invalid -> fail  )
                response.addProperty("responseCaptcha", responseCaptcha ? "success" : "fail");
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

}
