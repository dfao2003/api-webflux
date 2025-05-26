/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.controller;

import com.apiwebflux.demo.model.Auth;
import com.apiwebflux.demo.model.User;
import com.apiwebflux.demo.repository.AuthRepository;
import com.apiwebflux.demo.repository.IAuthRepository;
import com.apiwebflux.demo.repository.IUserRepository;
import com.apiwebflux.demo.repository.UserRepository;
import com.google.api.client.json.Json;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.concurrent.CompletableFuture;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author USUARIO
 */

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private IAuthRepository repository;

    @Autowired
    private IUserRepository repositoryUser;
    
    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody User user) {
        return repositoryUser.signIn(user) // devuelve Mono<String>
            .flatMap(token -> {
                if (token.contains("error")) {
                    JSONObject error = new JSONObject();
                    error.put("error", "Error al crear usuario");
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString()));
                }

                return Mono.fromCallable(() -> {
                    Firestore db = FirestoreClient.getFirestore();

                    ApiFuture<QuerySnapshot> future = db.collection("User")
                        .whereEqualTo("email", user.email).get();

                    QuerySnapshot querySnapshot = future.get(); // operación bloqueante

                    if (querySnapshot.isEmpty()) {
                        JSONObject error = new JSONObject();
                        error.put("error", "Usuario no encontrado en Firestore");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
                    }

                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                    JSONObject json = new JSONObject();
                    json.put("tokenId", token);
                    json.put("email", document.getString("email"));
                    json.put("name", document.getString("name"));
                    json.put("uid", document.getId());

                    System.out.println("CREACION DE USUARIO CORRECTO");

                    return ResponseEntity.ok(json.toString());
                }).subscribeOn(Schedulers.boundedElastic());
            })
            .onErrorResume(e -> {
                JSONObject error = new JSONObject();
                error.put("error", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString()));
            });
    }   
    
    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody Auth user) {
        return repository.login(user)
            .flatMap(token -> {
                if (token.contains("Error")) {
                    JSONObject error = new JSONObject();
                    error.put("error", "Token inválido");
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString()));
                }

                return Mono.fromCallable(() -> {
                    Firestore db = FirestoreClient.getFirestore();

                    ApiFuture<QuerySnapshot> future = db.collection("User")
                        .whereEqualTo("email", user.email).get();

                    QuerySnapshot querySnapshot = future.get(); // aún bloqueante

                    if (querySnapshot.isEmpty()) {
                        JSONObject error = new JSONObject();
                        error.put("error", "Usuario no encontrado en Firestore");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
                    }

                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                    JSONObject json = new JSONObject();
                    json.put("tokenId", token);
                    json.put("email", document.getString("email"));
                    json.put("name", document.getString("name"));
                    json.put("uid", document.getId());

                    System.out.println("INICIO DE SESION CORRECTO");
                    return ResponseEntity.ok(json.toString());

                }).subscribeOn(Schedulers.boundedElastic()); // mover operación bloqueante a un hilo aparte
            })
            .onErrorResume(e -> {
                JSONObject error = new JSONObject();
                error.put("error", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString()));
            });
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
            Runnable::run // Usa el mismo hilo
        );
        return completableFuture;
    }
    

    
}
