package com.apiwebflux.demo.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.apiwebflux.demo.environments.Data;
import com.apiwebflux.demo.model.User;
import com.apiwebflux.demo.service.FirebaseStorageService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Repository
public class UserRepository implements IUserRepository {

    private final WebClient webClient;
    private final FirebaseStorageService service;

    public UserRepository(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(Data.url).build();
        this.service = new FirebaseStorageService(); // Instanciar aquí una vez
    }

    @Override
    public Mono<String> signIn(User user) {
        // Paso 1: Crear usuario en Firebase (bloqueante → encapsular en Mono)
        return Mono.fromCallable(() -> {
            CreateRequest request = new CreateRequest()
                .setEmail(user.getEmail())
                .setPassword(user.getPassword());

            UserRecord record = FirebaseAuth.getInstance().createUser(request);

            Firestore db = FirestoreClient.getFirestore();
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            userData.put("photo", "");

            db.collection("User").document(record.getUid()).set(userData);


            return user;
        })
        .subscribeOn(Schedulers.boundedElastic()) // Ejecuta en hilo no bloqueante
        // Paso 2: Autenticación con WebClient
        .flatMap(u -> {
            Map<String, Object> requestBody = Map.of(
                "email", u.getEmail(),
                "password", u.getPassword(),
                "returnSecureToken", true
            );
            System.out.println("Creacion de usuario correcto");

            return webClient.post()
                .uri(Data.url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(token -> System.out.println(">> Firebase respondio"))
                .map(response -> new JSONObject(response).getString("idToken"))
                .doOnNext(token -> System.out.println(">> Token obtenido"))
                .timeout(Duration.ofSeconds(3)) // fail si tarda más 
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(500)));
        })
        .onErrorResume(e -> Mono.error(new RuntimeException("Error en signIn: " + e.getMessage())));
    }

    @Override
    public Mono<ResponseEntity<String>> modify(String email, String name, String photo) {
        return Mono.fromCallable(() -> {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("User")
                .whereEqualTo("email", email)
                .get();
            return future.get();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(querySnapshot -> {
            if (querySnapshot.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("error", "Usuario no encontrado en Firestore");
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString()));
            }

            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            String documentId = document.getId();

            return service.subirImagenBase64(name, photo) // ← Mono<String>
                .flatMap(newPhotoUrl -> Mono.fromCallable(() -> {
                    Firestore db = FirestoreClient.getFirestore(); // puede reutilizarse
                    ApiFuture<WriteResult> writeResult = db.collection("User")
                        .document(documentId)
                        .update("photo", newPhotoUrl);
                    writeResult.get(); // bloqueante

                    JSONObject json = new JSONObject();
                    json.put("photo", newPhotoUrl);
                    
                    return ResponseEntity.ok(json.toString());
                }).subscribeOn(Schedulers.boundedElastic()));
        });
    }

    
}
