package com.barclays.takehomecodingtest.config;

import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.AccountService;
import com.barclays.takehomecodingtest.service.AuthService;
import com.barclays.takehomecodingtest.service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockServiceConfiguration {

    @Bean
    @Primary
    public AuthService authService() {
        return Mockito.mock(AuthService.class);
    }

    @Bean
    @Primary
    public AccountService accountService() {
        return Mockito.mock(AccountService.class);
    }

    @Bean
    @Primary
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }
}
