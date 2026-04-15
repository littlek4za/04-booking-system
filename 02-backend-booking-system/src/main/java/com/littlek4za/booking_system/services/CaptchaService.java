package com.littlek4za.booking_system.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.littlek4za.booking_system.dtos.CaptchaResponseDto;


@Service
public class CaptchaService {

    private WebClient webClient;

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    public CaptchaService(WebClient.Builder wBuilder) {
        this.webClient = wBuilder.baseUrl("https://www.google.com").build(); // set default baseUrl
    }

    public boolean verify(String captchaToken){
        CaptchaResponseDto responseDto = webClient.post() // method
                .uri("recaptcha/api/siteverify") 
                .contentType(MediaType.APPLICATION_FORM_URLENCODED) // one type of format secret=xxx&response=yyy
                .body(BodyInserters.fromFormData("secret",secretKey)
                .with("response",captchaToken))
                .retrieve() // excute and get response
                .bodyToMono(CaptchaResponseDto.class) // convert to jave object
                .block(); // tunr asyn Mono<RecaptchaResponse> to RecaptchaResponse
        
        return responseDto != null && Boolean.TRUE.equals(responseDto.success());
    }

}
