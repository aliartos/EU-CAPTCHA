package com.sii.eucaptcha.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "controller.captcha")
public class ControllerConfigProperties {
}
