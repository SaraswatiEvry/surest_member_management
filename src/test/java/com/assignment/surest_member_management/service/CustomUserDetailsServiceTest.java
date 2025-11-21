package com.assignment.surest_member_management.service;

import com.assignment.surest_member_management.entity.Role;
import com.assignment.surest_member_management.entity.User;
import com.assignment.surest_member_management.repository.UserRepository;
import com.assignment.surest_member_management.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername() throws UsernameNotFoundException {
        Role role = new Role(UUID.randomUUID(), "ROLE_USER");
        User user = new User(UUID.randomUUID(), "test", "hashedPassword", role);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("test");

        assertEquals("test", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_USER")));
    }
}
