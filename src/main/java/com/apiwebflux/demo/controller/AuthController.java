/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.controller;

import com.apiwebflux.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author USUARIO
 */

@RestController
@RequestMapping("/api")
public class AuthController {
    
    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody User user){
        
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        //userMap.put("password", user.getPassword());
        userMap.put("name", user.getNombreUsuario());
        
        ApiFuture<WriteResult> future = db.collection("users").document(user.getEmail()).set(userMap);
        
        return Mono.fromFuture(toCompletableFuture(future))
                .map(writeResult -> ResponseEntity.ok("Usuario guardado correctamente en Firestore"))
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(500).body("Error al guardar usuario: " + e.getMessage()));
                });                
    }
    
    
    @PostMapping
    public Mono<ResponseEntity<String>> login(@RequestBody User user){
        
        Firestore db = FirestoreClient.getFirestore();
        
        return Mono.fromFuture(toCompletableFuture(
            db.collection("users").document(user.getEmail()).get()
        )).map(snapshot -> {
            if (!snapshot.exists()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }
            String storedCorreo = snapshot.getString("email");
            // Comparar la contraseña enviada con la almacenada (sin encriptar por ahora)
            if (storedCorreo != null && storedCorreo.equals(user.getEmail())) {
                // En un sistema real aquí deberías generar un JWT propio o devolver el UID
                return ResponseEntity.ok("Login exitoso");
            } else {
                return ResponseEntity.status(401).body("Contraseña incorrecta");
            }
        }).onErrorResume(e ->
            Mono.just(ResponseEntity.status(500).body("Error al procesar login: " + e.getMessage()))
        );        
        
    }
    
    
    @GetMapping("/profile")
    public Mono<ResponseEntity<String>> getProfile(ServerWebExchange exchange){
        String uid = (String)exchange.getAttribute("uid");
        if (uid != null) {
            return Mono.just(ResponseEntity.ok("Usuario autenticado con UID: " + uid));
        } else {
            return Mono.just(ResponseEntity.status(401).body("No autorizado"));
        }
    }
    
    private <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        com.google.api.core.ApiFutures.addCallback(
            apiFuture,
            new com.google.api.core.ApiFutureCallback<>() {
                @Override
                public void onSuccess(T result) {
                    completableFuture.complete(result);
                }
                @Override
                public void onFailure(Throwable t) {
                    completableFuture.completeExceptionally(t);
                }
            },
            Runnable::run // ejecuta en el mismo hilo
        );
        return completableFuture;
    }
    
}
