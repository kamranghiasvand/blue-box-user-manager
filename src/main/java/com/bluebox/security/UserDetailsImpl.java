package com.bluebox.security;

import com.bluebox.service.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String uid;
    private String username;
    @JsonIgnore
    private String password;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialNonExpired = true;
    private boolean enabled = true;

    private final Collection<? extends GrantedAuthority> authorities = new ArrayList<>();

    private UserDetailsImpl() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static UserDetailsImpl build(final UserEntity user) {
        var details = new UserDetailsImpl();
        details.id = user.getId();
        details.uid = user.getUuid();
        details.enabled = user.getEnabled();
        details.password = user.getPassword();
        details.username = user.getEmail();
        details.accountNonExpired = true;
        details.accountNonLocked = true;
        details.credentialNonExpired = true;
        return details;
    }
}
