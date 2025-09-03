package kr.co.hanipcloudgateway.configuration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
/*
ServerAuthenticationEntryPoint는 Security를 통한 인증 실패 시 호출되며, 이 메서드에서 예외를 발생시켜 ErrorWebExceptionHandler로 전달할 수 있습니다.
이렇게 하면 AuthenticationException이 발생하고, 이를 글로벌 예외 처리기에서 처리할 수 있습니다.
*/
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return Mono.error(ex);
    }
}