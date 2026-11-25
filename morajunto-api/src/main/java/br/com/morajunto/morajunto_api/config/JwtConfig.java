package br.com.morajunto.morajunto_api.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(@Value("${morajunto.jwt.secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(criarChave(secret)));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${morajunto.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(criarChave(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private SecretKey criarChave(String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}