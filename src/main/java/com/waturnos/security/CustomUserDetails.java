package com.waturnos.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;
	private final Long id;
    private final String email;
    private final String password;
    private Long organizationId;
    private String organizationName;
    private Boolean simpleOrganization;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        this.active = user.getActive();
        if(UserRole.MANAGER.equals(user.getRole()) || UserRole.PROVIDER.equals(user.getRole())) {
        	this.organizationName = user.getOrganization().getName();
        	this.organizationId = user.getOrganization().getId();
        	this.simpleOrganization = user.getOrganization().isSimpleOrganization();
        }
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
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}