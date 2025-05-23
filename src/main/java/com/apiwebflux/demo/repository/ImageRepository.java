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


@Repository
public class ImageRepository implements IImageRepository{

    private FirebaseStorageService service;

    @Override
    public String publicPost(Post post) {
        try{

            Firestore db = FirestoreClient.getFirestore();
            service = new FirebaseStorageService();

            System.out.println("Subiendo foto");
            String url = service.subirImagenBase64(post.name, post.photo);


            System.out.println(post.name);
            System.out.println(post.photo);

            Map<String, Object> userData = new HashMap<>();

            Timestamp timestamp = Timestamp.of(post.getCreated_at());

            userData.put("description", post.description);
            userData.put("uid_user", post.uid_user);
            userData.put("url", url);
            userData.put("created_at", timestamp);
            

            db.collection("Post").add(userData);

            return "True";

        }catch(Exception e){
            System.err.println(e.getMessage());
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    @Override
    public String applyFilter(ImagenRequest request)  {

        System.out.println("Entrando en metodo");
        RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> flaskBody = new HashMap<>();
            flaskBody.put("image", request.getBase64());

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(flaskBody, headers);

            //System.out.println(request.getBase64());
            String flaskUrl = "http://localhost:5000/"+ request.getFilter(); // o la IP real si es externa

            ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, requestEntity, Map.class);

            System.out.println("Realizando POST");
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("Exito");
                String processedImage = response.getBody().get("imagen").toString();
                //System.out.println(processedImage);
                return processedImage;
            } else {
                return HttpStatus.INTERNAL_SERVER_ERROR.toString();
            }

            
        //throw new UnsupportedOperationException("Unimplemented method 'applyFilter'");
    }
    
}
