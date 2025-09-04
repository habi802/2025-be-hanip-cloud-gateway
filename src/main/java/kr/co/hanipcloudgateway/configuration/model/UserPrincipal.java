package kr.co.hanipcloudgateway.configuration.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class UserPrincipal implements UserDetails {
    private final long signedUserId;
    private final Collection<? extends GrantedAuthority> role;


    public UserPrincipal(long signedUserId, String role) {
        this.signedUserId = signedUserId;
        this.role = List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role;
    }
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(signedUserId);
    }
}
