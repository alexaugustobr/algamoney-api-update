package com.example.algamoney.api.config;

import com.example.algamoney.api.config.property.AlgamoneyApiProperty;
import com.example.algamoney.api.model.Usuario;
import com.example.algamoney.api.repository.UsuarioRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwsEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.io.InputStream;
import java.security.KeyStore;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Profile("oauth-security")
public class AuthServerConfig {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AlgamoneyApiProperty algamoneyApiProperty;

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient angularClient = RegisteredClient
                .withId("1")
                .clientId("angular")
                .clientSecret(passwordEncoder.encode("@ngul@r0"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUris(uris -> uris.addAll(algamoneyApiProperty.getSeguranca().getRedirectsPermitidos()))
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(24))
                        .build())
                .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .build())
                .build();

        RegisteredClient mobileClient = RegisteredClient
                .withId("2")
                .clientId("mobile")
                .clientSecret(passwordEncoder.encode("m0b1le"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUris(uris -> uris.addAll(algamoneyApiProperty.getSeguranca().getRedirectsPermitidos()))
                .scope("read")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(24))
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();


        JdbcRegisteredClientRepository registeredClientRepository =
                new JdbcRegisteredClientRepository(jdbcTemplate);
        
        registeredClientRepository.save(angularClient);
        registeredClientRepository.save(mobileClient);

        return registeredClientRepository;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.formLogin(Customizer.withDefaults()).build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtBuildCustomizer(UsuarioRepository usuarioRepository) {
        return (context) -> {
            Authentication authenticationToken = context.getPrincipal();
            User userSpring = (User) authenticationToken.getPrincipal();

            Set<String> authorities = new HashSet<>();
            for (GrantedAuthority grantedAuthority : userSpring.getAuthorities()) {
                authorities.add(grantedAuthority.getAuthority());
            }

            //Consulta s?? ?? chamada uma vez durante a customiza????o do Token
            final Usuario usuario = usuarioRepository.findByEmail(userSpring.getUsername()).orElseThrow();

            context.getClaims().claim("nome", usuario.getNome());
            context.getClaims().claim("authorities", authorities);
        };
    }

    @Bean
    public JWKSet jwkSet() throws Exception {
        final InputStream inputStream = new ClassPathResource("keystore/algamoney.jks").getInputStream();

        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(inputStream, "123456".toCharArray());

        RSAKey rsaKey = RSAKey.load(
                keyStore,
                "algamoney",
                "123456".toCharArray()
        );

        return new JWKSet(rsaKey);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwsEncoder(jwkSource);
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder()
                .issuer(algamoneyApiProperty.getSeguranca().getAuthServerUrl())
                .build();
    }

}
