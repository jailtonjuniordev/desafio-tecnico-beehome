package com.jjdev.beehome_tecnico.auth.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

	private final CorsProperties corsProperties;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		List<String> origins = sanitizeOrigins(corsProperties.allowedOrigins());

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(origins);
		config.setAllowedMethods(corsProperties.allowedMethods());
		config.setAllowedHeaders(corsProperties.allowedHeaders());
		config.setAllowCredentials(corsProperties.allowCredentials());
		config.setMaxAge(corsProperties.maxAge());

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	private static List<String> sanitizeOrigins(List<String> origins) {
		if (origins == null) {
			return List.of();
		}
		return origins.stream()
				.filter(origin -> origin != null && !origin.isBlank())
				.map(String::trim)
				.toList();
	}
}
