package kr.co.hanipcloudgateway.configuration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class JwtUser {
    private final long signedUserId;
    private final String role;
}
