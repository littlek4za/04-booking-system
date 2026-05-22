package com.littlek4za.booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

import org.springframework.beans.factory.annotation.Value;


@Configuration
public class ResendConfig {

    @Value("${resend.api.key}")
    private String resendAPI;

    @Bean
    public Resend resend(){
        return new Resend(resendAPI);
    }

}
