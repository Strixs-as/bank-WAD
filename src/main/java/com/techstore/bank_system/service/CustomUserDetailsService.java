package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        System.out.println("CustomUserDetailsService: Loading user " + username + " with roles subset size " + (user.getRoles() != null ? user.getRoles().size() : "null"));

        // Если ролей нет — по умолчанию USER
        List<GrantedAuthority> authorities;
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            System.out.println("CustomUserDetailsService: Assigned default ROLE_USER");
        } else {
            authorities = user.getRoles().stream()
                    .map(role -> {
                        String rName = role.getName();
                        if(!rName.startsWith("ROLE_")) {
                            rName = "ROLE_" + rName;
                        }
                        return new SimpleGrantedAuthority(rName);
                    })
                    .collect(Collectors.toList());
            System.out.println("CustomUserDetailsService: Assigned roles " + authorities);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
