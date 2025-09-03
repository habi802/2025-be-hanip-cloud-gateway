package kr.co.hanipcloudgateway.configuration.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hanipcloudgateway.configuration.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// 내부(gateway) 에러 처리
@Slf4j
@Order(-1)
@RequiredArgsConstructor
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Header
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException)ex).getStatusCode());
        }

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                String message = response.getStatusCode() == HttpStatus.UNAUTHORIZED ? "로그인해 주세요." : ex.getMessage();
                ResultResponse<?> resultResponse = ResultResponse.<Void>builder().message(message).build();
                byte[] errorResponse = objectMapper.writeValueAsBytes(resultResponse);
                return bufferFactory.wrap(errorResponse);
            } catch (Exception e) {
                log.error("error", e);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
