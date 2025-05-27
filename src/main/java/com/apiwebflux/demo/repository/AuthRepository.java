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
import reactor.core.publisher.Mono;

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


        
        return webClient.post()
            .uri("")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                JSONObject json = new JSONObject(response);
                return json.getString("idToken");
            })
            .onErrorResume(e -> Mono.error(new RuntimeException("Error en login Firebase: " + e.getMessage())));
    }
    
}
