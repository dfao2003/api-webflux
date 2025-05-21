/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.controller;

import com.apiwebflux.demo.model.Auth;
import com.apiwebflux.demo.model.User;
import com.apiwebflux.demo.repository.AuthRepository;
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

/**
 *
 * @author USUARIO
 */

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthRepository repository;
    
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
    
    
    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody Auth user) {
        try {
            String token = repository.login(user); 
            JSONObject json = null;
            if(!token.contains("Error")){
                json = new JSONObject();
                json.put("tokenId", token);
                Firestore db = FirestoreClient.getFirestore();

                ApiFuture<QuerySnapshot> future = db.collection("User").whereEqualTo("email", user.email).get();
                QuerySnapshot querySnapshot = future.get(); // bloqueante pero dentro del Callable

                if (querySnapshot.isEmpty()) {
                    JSONObject error = new JSONObject();
                    error.put("error", "Usuario no encontrado en Firestore");
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString()));
                }

                DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                json.put("email", document.getString("email"));
                json.put("name", document.getString("name"));
                json.put("uid", document.getId());
            }

            return Mono.just(ResponseEntity.ok(json.toString()));
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error.toString()));
        }
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
