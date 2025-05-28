package com.apiwebflux.demo.repository;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.apiwebflux.demo.environments.Data;
import com.apiwebflux.demo.model.Auth;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Repository
public class AuthRepository implements IAuthRepository{

    private final WebClient webClient;

    public AuthRepository(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(Data.url).build();
    }

    @Override
    public Mono<String> login(Auth user) {
        Map<String, Object> requestBody = Map.of(
            "email", user.email,
            "password", user.password,
            "returnSecureToken", true
        );
        System.out.println("Realizando autenticacion");
        return webClient.post()
        .uri(Data.url)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .doOnNext(r -> System.out.println(">> Firebase respondió"))
        .map(response -> new JSONObject(response).getString("idToken"))
        .doOnNext(token -> System.out.println(">> Token obtenido"))
        .timeout(Duration.ofSeconds(3)) // fail si tarda más 
        .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(500)))
        .onErrorResume(e -> {
            System.out.println(">> Error en login: " + e.getMessage());
            return Mono.error(new RuntimeException("Error en login Firebase: " + e.getMessage()));
        });

    }
    
}
