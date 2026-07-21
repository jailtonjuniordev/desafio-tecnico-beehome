package com.jjdev.beehome_tecnico.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.jjdev.beehome_tecnico.auth.infrastructure.config.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtServiceUtils {

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final JwtProperties jwtProperties;

	public JwtServiceUtils(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		SecretKey key = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
		this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	public String generate(UUID userId, String email) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(userId.toString())
				.claim("email", email)
				.issuedAt(now)
				.expiresAt(now.plusMillis(jwtProperties.expirationMs()))
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public Jwt validate(String token) {
		return jwtDecoder.decode(token);
	}

	public UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new IllegalStateException("Authenticated JWT user not found");
		}
		return UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
	}

	public long getExpirationMs() {
		return jwtProperties.expirationMs();
	}
}
