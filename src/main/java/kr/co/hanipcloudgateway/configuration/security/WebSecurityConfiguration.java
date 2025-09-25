package kr.co.hanipcloudgateway.configuration.security;

//Spring Security 세팅

import kr.co.hanipcloudgateway.configuration.exception.CustomAuthenticationEntryPoint;
import kr.co.hanipcloudgateway.configuration.jwt.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration //메소드 빈등록이 있어야 의미가 있다. 메소드 빈등록이 싱글톤이 됨.
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration {
    private final Environment environment;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String[] activeProfiles = environment.getActiveProfiles();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);

        if (Arrays.asList(activeProfiles).contains("prod")) {
            configuration.addAllowedOrigin("https://greenart.n-e.kr");
        } else {
            configuration.addAllowedOrigin("http://localhost:5173");
            // configuration.setAllowedOriginPatterns(List.of("*"));
        }

        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                   .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                   .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                   .securityContextRepository(new StatelessWebSessionSecurityContextRepository()) // 세션 사용 안 함
                   .authorizeExchange(exchanges -> exchanges.pathMatchers("/api/user/login", "/api/user/join").permitAll()
                                                            .anyExchange().permitAll()
                   )
                   .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                   .addFilterAt(tokenAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                   .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint))
                   .build();
    }

    //https://gose-kose.tistory.com/27
    private static class StatelessWebSessionSecurityContextRepository implements ServerSecurityContextRepository {

        private static final Mono<SecurityContext> EMPTY_CONTEXT = Mono.empty();

        @Override
        public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
            return Mono.empty();
        }

        @Override
        public Mono<SecurityContext> load(ServerWebExchange exchange) {
            return EMPTY_CONTEXT;
        }
    }
}
