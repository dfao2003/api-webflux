/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.security;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 *
 * @author USUARIO
 */

@Component
public class FirebaseAuthenticationFilter implements WebFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of("/api/login", "/api/signup", "/img/filter");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange); // ruta pública
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7).trim();

            return Mono.fromFuture(toCompletableFuture(FirebaseAuth.getInstance().verifyIdTokenAsync(idToken)))
                .flatMap(decodedToken -> {
                    exchange.getAttributes().put("uid", decodedToken.getUid());
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    System.out.println("Token inválido: " + e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        ApiFutures.addCallback(apiFuture, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(T result) {
                completableFuture.complete(result);
            }
            @Override
            public void onFailure(Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        }, Runnable::run);
        return completableFuture;
    }
}
