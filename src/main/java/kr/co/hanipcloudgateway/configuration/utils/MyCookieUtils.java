package kr.co.hanipcloudgateway.configuration.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MyCookieUtils {

    //Req header에서 내가 원하는 쿠키를 찾는 메소드
    public String getValue(ServerHttpRequest req, String name) {
        List<HttpCookie> cookies = req.getCookies().get(name);

        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        return cookies.get(0).getValue();
    }

}
