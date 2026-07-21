package com.jjdev.beehome_tecnico.shared.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.jjdev.beehome_tecnico.auth.infrastructure.config.CorsProperties;
import com.jjdev.beehome_tecnico.auth.infrastructure.config.JwtProperties;

@Configuration
@EnableConfigurationProperties({
        CorsProperties.class,
        JwtProperties.class})
public class ApplicationConfiguration {
}