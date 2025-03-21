package com.sii.eucaptcha.controller.dto.captcharesult;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

public class CaptchaResultDto implements Serializable {

    @Schema(
            description = "Generated ID of the Captcha",
            name = "captchaId",
            type = "string",
            example = "mj0kvg8s39sufq9uj8cs5ckorj"
    )
    private String captchaId ;
    @Schema(
            description = "The CaptchaImage",
            name = "captchaImg",
            type = "string",
            example = "iVBORw0KGgoAAAANSUhEUgAAAZAAAADICAIAAABJdyC//..."
    )
    private String captchaImg ;
    @Schema(
            description = "Type of the Captcha",
            name = "captchaType",
            type = "string",
            example = "STANDARD for textual captcha or WHATS_UP for rotated image captcha"
    )
    private String captchaType = CaptchaConstants.STANDARD;

    @Schema(
            description = "The Captcha question",
            name = "captchaQuestion",
            type = "string",
            example = ""
    )
    private String captchaQuestion;

    private int max;

    private int min;

    public CaptchaResultDto() {
        super();
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaImg() {
        return captchaImg;
    }

    public void setCaptchaImg(String captchaImg) {
        this.captchaImg = captchaImg;
    }

    public String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        this.captchaType = captchaType;
    }

    public String getCaptchaQuestion() {
        return captchaQuestion;
    }

    public void setCaptchaQuestion(String captchaQuestion) {
        this.captchaQuestion = captchaQuestion;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

}
