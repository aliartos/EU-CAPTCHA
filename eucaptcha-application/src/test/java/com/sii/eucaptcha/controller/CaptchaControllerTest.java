package com.sii.eucaptcha.controller;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import com.sii.eucaptcha.controller.dto.captchaquery.CaptchaQueryDto;
import com.sii.eucaptcha.controller.dto.captcharesult.CaptchaResultDto;
import com.sii.eucaptcha.controller.dto.captcharesult.TextualCaptchaResultDtoDto;
import com.sii.eucaptcha.service.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CaptchaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private CaptchaController captchaController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController).build();
    }

    private static final String TEST_JWT_TOKEN = "test-jwt-token";

    @DisplayName("Test getting Captcha")
    @Test
    public void testGetCaptcha() throws Exception {
        // Arrange
        TextualCaptchaResultDtoDto captchaResult = new TextualCaptchaResultDtoDto();
        captchaResult.setCaptchaId("testCaptchaId");
        captchaResult.setCaptchaImg("testCaptchaImage");
        captchaResult.setAudioCaptcha("testAudioCaptcha");
        captchaResult.setCaptchaType(CaptchaConstants.STANDARD);

        when(captchaService.generateCaptchaWrapper(any(CaptchaQueryDto.class))).thenReturn(captchaResult);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/captchaImg")
                .param("locale", "en")
                .param("captchaType", CaptchaConstants.STANDARD)
                .header("xJwtString", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captchaId").value("testCaptchaId"))
                .andExpect(jsonPath("$.captchaImg").value("testCaptchaImage"))
                .andExpect(jsonPath("$.audioCaptcha").value("testAudioCaptcha"))
                .andExpect(jsonPath("$.captchaType").value(CaptchaConstants.STANDARD));
    }

    @DisplayName("Test reloading Captcha")
    @Test
    public void testReloadCaptcha() throws Exception {
        // Arrange
        String previousCaptchaId = "previousCaptchaId";
        TextualCaptchaResultDtoDto captchaResult = new TextualCaptchaResultDtoDto();
        captchaResult.setCaptchaId("newCaptchaId");
        captchaResult.setCaptchaImg("newCaptchaImage");
        captchaResult.setAudioCaptcha("newAudioCaptcha");
        captchaResult.setCaptchaType(CaptchaConstants.STANDARD);

        when(captchaService.generateCaptchaWrapper(any(CaptchaQueryDto.class))).thenReturn(captchaResult);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/reloadCaptchaImg/{previousCaptchaId}", previousCaptchaId)
                .param("locale", "en")
                .param("captchaType", CaptchaConstants.STANDARD)
                .header("xJwtString", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captchaId").value("newCaptchaId"))
                .andExpect(jsonPath("$.captchaImg").value("newCaptchaImage"))
                .andExpect(jsonPath("$.audioCaptcha").value("newAudioCaptcha"))
                .andExpect(jsonPath("$.captchaType").value(CaptchaConstants.STANDARD));
    }

    @DisplayName("Test validating Captcha with valid answer")
    @Test
    public void testValidateCaptchaWithValidAnswer() throws Exception {
        // Arrange
        String captchaId = "validCaptchaId";
        String captchaAnswer = "correctAnswer";

        when(captchaService.validateCaptcha(anyString(), eq(captchaId), eq(captchaAnswer), anyString(), anyBoolean())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/validateCaptcha/{captchaId}", captchaId)
                .param("captchaAnswer", captchaAnswer)
                .param("captchaType", CaptchaConstants.STANDARD)
                .header("xJwtString", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @DisplayName("Test validating Captcha with invalid answer")
    @Test
    public void testValidateCaptchaWithInvalidAnswer() throws Exception {
        // Arrange
        String captchaId = "validCaptchaId";
        String captchaAnswer = "wrongAnswer";

        when(captchaService.validateCaptcha(anyString(), eq(captchaId), eq(captchaAnswer), anyString(), anyBoolean())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/validateCaptcha/{captchaId}", captchaId)
                .param("captchaAnswer", captchaAnswer)
                .param("captchaType", CaptchaConstants.STANDARD)
                .header("xJwtString", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
