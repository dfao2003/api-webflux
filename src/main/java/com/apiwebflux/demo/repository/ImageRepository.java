package com.apiwebflux.demo.repository;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.apiwebflux.demo.environments.Data;
import com.apiwebflux.demo.model.ImagenRequest;
import com.apiwebflux.demo.model.Post;
import com.apiwebflux.demo.service.FirebaseStorageService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Repository
public class ImageRepository implements IImageRepository {

    private final WebClient webClient;
    private final FirebaseStorageService service;

    public ImageRepository(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://api-pycuda:5000").build(); // Base para los filtros
        this.service = new FirebaseStorageService(); // Instanciar aquí una vez
    }

    @Override
    public Mono<String> publicPost(Post post) {
        Firestore db = FirestoreClient.getFirestore();

        return service.subirImagenBase64(post.name, post.photo)
            .flatMap(url -> Mono.fromCallable(() -> {
                Map<String, Object> userData = new HashMap<>();
                Timestamp timestamp = Timestamp.of(post.getCreated_at());

                userData.put("description", post.description);
                userData.put("uid_user", post.uid_user);
                userData.put("url", url);
                userData.put("created_at", timestamp);

                db.collection("Post").add(userData); // operación bloqueante

                return "True";
            }).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.error(new RuntimeException("Error en publicPost: " + e.getMessage())));
}
    @Override
    public Mono<String> applyFilter(ImagenRequest request) {
        Map<String, String> flaskBody = Map.of("image", request.getBase64());

        return webClient.post()
            .uri("/" + request.getFilter())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(flaskBody)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .map(response -> response.get("imagen").toString())
            .onErrorResume(e -> Mono.error(new RuntimeException("Error aplicando filtro: " + e.getMessage())));
    }
}
