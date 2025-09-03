package kr.co.hanipcloudgateway.configuration.security;

//Spring Security 세팅

import kr.co.hanipcloudgateway.configuration.exception.CustomAuthenticationEntryPoint;
import kr.co.hanipcloudgateway.configuration.jwt.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(
                Arrays.asList("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean //스프링이 메소드 호출을 하고 리턴한 객체의 주소값을 관리한다. (빈등록)
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(new StatelessWebSessionSecurityContextRepository()) // 세션 사용 안 함
                .authorizeExchange(exchanges -> exchanges.pathMatchers("/api/feed", "/api/feed/**").authenticated()
                        .pathMatchers(HttpMethod.GET,"/api/user").authenticated()
                        .pathMatchers(HttpMethod.PATCH,"/api/user/pic").authenticated()
                        .anyExchange().permitAll()
                )
                .cors(corsSpec -> corsConfigurationSource())
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
