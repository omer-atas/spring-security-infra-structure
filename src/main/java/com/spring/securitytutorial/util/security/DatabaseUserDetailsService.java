package com.spring.securitytutorial.util.security;

import com.spring.securitytutorial.service.model.user.UserAccount;
import com.spring.securitytutorial.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount account = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var authorities = Stream.concat(account.roles().stream(), account.permissions().stream())
                .map(SimpleGrantedAuthority::new).toList();
        return User.withUsername(account.username()).password(account.passwordHash())
                .disabled(!account.enabled()).authorities(authorities).build();
    }
}