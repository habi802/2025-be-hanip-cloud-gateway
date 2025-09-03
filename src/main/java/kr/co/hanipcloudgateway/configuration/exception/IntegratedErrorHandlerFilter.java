package kr.co.hanipcloudgateway.configuration.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hanipcloudgateway.configuration.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegratedErrorHandlerFilter implements WebFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        //응답 데이터를 가로채기 위해서 사용
        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                if (isErrorCase(exchange)) {
                    if (body instanceof Flux) {
                        ServerHttpResponse response = exchange.getResponse();
                        DataBufferFactory dataBufferFactory = response.bufferFactory();

                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                        return super.writeWith(fluxBody.buffer().handle((dataBuffers, sink) -> {
                            DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
                            byte[] content = new byte[joinedBuffers.readableByteCount()];
                            joinedBuffers.read(content);

                            String responseBody = new String(content, StandardCharsets.UTF_8);
                            log.info("response body: {}", responseBody);

                            try {
                                Map<String, String> responseMap = objectMapper.readValue(responseBody, Map.class);
                                ResultResponse rr = ResultResponse.<Void>builder()
                                        .message(responseMap.get("message"))
                                        .build();
                                sink.next(dataBufferFactory.wrap(objectMapper.writeValueAsBytes(rr)));
                            } catch (Exception e) {
                                sink.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                            }
                        }));
                    }
                }
                return getDelegate().writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }

    private boolean isErrorCase(ServerWebExchange exchange) {
        return exchange.getResponse().getStatusCode() != HttpStatus.OK;
    }
}
