package com.sii.eucaptcha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.util.concurrent.ConcurrentHashMap;

@EnableSpringHttpSession
@SpringBootApplication
@Slf4j
public class EuCaptchaApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EuCaptchaApplication.class);
	}
	public static void main(String[] args) {
		SpringApplication.run(EuCaptchaApplication.class, args);
	}

	@Bean
	CookieSerializer cookieSerializer() {
		DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
		defaultCookieSerializer.setCookieName("JSESSIONID");
		defaultCookieSerializer.setUseHttpOnlyCookie(true);
		defaultCookieSerializer.setCookiePath("/");
		defaultCookieSerializer.setUseSecureCookie(true);
		defaultCookieSerializer.setSameSite("none");
		return defaultCookieSerializer;
	}

	@Bean
	public MapSessionRepository sessionRepository() {
		return new MapSessionRepository(new ConcurrentHashMap<>());
	}
}
