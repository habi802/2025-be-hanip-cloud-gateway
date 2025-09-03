package kr.co.hanipcloudgateway.configuration.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UTF8EncodingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 확장자 기반으로 이미지 요청인지 판별 (jpg, png, gif, webp, svg 등)
        // 이미지 응답일 경우는 인코딩을 변경하지 않는다 .
        if (!path.matches(".*\\.(?i)(jpg|jpeg|png|gif|bmp|webp|svg)$")) {
            exchange.getResponse().getHeaders().set("Content-Type", "application/json;charset=UTF-8");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // 우선순위 (높을수록 먼저 실행)
    }
}
