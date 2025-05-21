/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.service;

import com.apiwebflux.demo.model.ImageResponse;
import com.apiwebflux.demo.model.ImagenRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * @author USUARIO
 */
@Service
public class FirebasePycudaAPI {
    
    private final WebClient webClient;

    public FirebasePycudaAPI(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:5001").build();
    }

    public Mono<String> enviarImagen(String base64) {
        ImagenRequest request = ImagenRequest.builder().base64(base64).build();

        return webClient.post()
                .uri("/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ImageResponse.class)
                .map(ImageResponse::getResultado_base64);
    }
    
}
