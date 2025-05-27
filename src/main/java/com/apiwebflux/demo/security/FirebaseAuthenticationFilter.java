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
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 *
 * @author USUARIO
 */
@Component
public class FirebaseAuthenticationFilter implements WebFilter{

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        
        //System.out.println("Pasando por filtro");
        String path = exchange.getRequest().getPath().toString();

        // EXCEPCIONES: rutas públicas que no requieren token
        if (path.equals("/api/login") || path.equals("/api/signup") || path.equals("/img/filter")) {
            return chain.filter(exchange); // pasa sin validar token
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        //System.out.println("HEADER RECIBIDO: " + authHeader); // Debug
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7).trim();
            //System.out.println("TOKEN EXTRAÍDO: " + idToken); // Debug
        
            return Mono.fromFuture(toCompletableFuture(FirebaseAuth.getInstance().verifyIdTokenAsync(idToken)))
                .flatMap(decodedToken -> {
                    System.out.println("TOKEN VERIFICADO UID: " + decodedToken.getUid()); 
                    exchange.getAttributes().put("uid", decodedToken.getUid());
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    System.out.println("ERROR AL VERIFICAR TOKEN: " + e.getMessage()); 
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
        }
                
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7);
            return Mono.fromFuture(toCompletableFuture(FirebaseAuth.getInstance().verifyIdTokenAsync(idToken)))
                .flatMap(decodedToken -> {
                    exchange.getAttributes().put("uid", decodedToken.getUid());
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

            
    private <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        ApiFutures.addCallback(
            apiFuture,
            new ApiFutureCallback<>() {
                @Override
                public void onSuccess(T result) {
                    completableFuture.complete(result);
                }
                @Override
                public void onFailure(Throwable t) {
                    completableFuture.completeExceptionally(t);
                }
            },
            Runnable::run
        );
        return completableFuture;
    }
    
}
