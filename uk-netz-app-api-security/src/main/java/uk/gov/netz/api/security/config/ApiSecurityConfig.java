package uk.gov.netz.api.security.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import uk.gov.netz.api.authorization.core.service.AuthorityService;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.security.ApplicationAuthenticationConverter;
import uk.gov.netz.api.security.RoleTypeClaimConverter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class ApiSecurityConfig {

    private final AuthorityService<?> authorityService;
    private final UserRoleTypeService userRoleTypeService;
    private final SecurityProperties securityProperties;

    public ApiSecurityConfig(AuthorityService<?> authorityService, UserRoleTypeService userRoleTypeService, SecurityProperties securityProperties) {
        this.authorityService = authorityService;
        this.userRoleTypeService = userRoleTypeService;
        this.securityProperties = securityProperties;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher[] unauthenticatedRequestMatchers = securityProperties.getUnauthenticatedApis().stream()
                .map(AntPathRequestMatcher::antMatcher)
                .toList()
                .toArray(RequestMatcher[]::new);

        http
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.httpStrictTransportSecurity(hstsConfig -> hstsConfig.includeSubDomains(true)))
                .authorizeHttpRequests(authorize -> authorize.requestMatchers(unauthenticatedRequestMatchers).permitAll())
                .authorizeHttpRequests(authorize -> authorize.requestMatchers(antMatcher("/**")).authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(new ApplicationAuthenticationConverter(authorityService))));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(
                properties.getJwt().getJwkSetUri()).build();

        jwtDecoder.setClaimSetConverter(new RoleTypeClaimConverter(userRoleTypeService));
        return jwtDecoder;
    }
}
