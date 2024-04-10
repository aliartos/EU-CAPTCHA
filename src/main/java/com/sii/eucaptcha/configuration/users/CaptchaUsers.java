package com.sii.eucaptcha.configuration.users;

import com.sii.eucaptcha.service.CaptchaService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by: Eddy Hoevenaers
 * Created on: dinsdag 09 april 2024
 */

@Getter
@Setter
@Component
public class CaptchaUsers {

    private Map<String, String> validUsers;

    public boolean isNoValidUser(String key) {
        return !validUsers.containsKey(key);
    }

    public String getUserValue(String key) {
            return validUsers.get(key);
    }

}
