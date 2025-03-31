package com.sii.eucaptcha.configuration.users;

import com.sii.eucaptcha.service.CaptchaService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by: Eddy Hoevenaers
 * Created on: dinsdag 09 april 2024
 */

@Getter
@Setter
@Component
@Slf4j
public class CaptchaUsers {

    private Map<String, String> validUsers;

    @PostConstruct
    public void init() {
        if (validUsers == null) {
            log.info("Initializing validUsers with empty map. This is expected when AWS integration is disabled.");
            validUsers = new HashMap<>();
        }
    }

    public boolean isNoValidUser(String key) {
        return !validUsers.containsKey(key);
    }

    public String getUserValue(String key) {
            return validUsers.get(key);
    }

}
