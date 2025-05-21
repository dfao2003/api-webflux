package com.apiwebflux.demo.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.apiwebflux.demo.environments.Data;
import com.apiwebflux.demo.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.cloud.FirestoreClient;

@Repository
public class UserRepository implements IUserRepository {

    FirebaseAuth auth;

    @Override
    public String signIn(User user) throws FirebaseAuthException, MalformedURLException, IOException {

        CreateRequest request = new CreateRequest()
            .setEmail(user.getEmail())
            .setPassword(user.getPassword());
            
        UserRecord record = FirebaseAuth.getInstance().createUser(request);

        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());

        db.collection("User").document(record.getUid()).set(userData);

        ///autenticacion para devolver el token
        /// 
        String requestBody = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", user.email, user.password
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(Data.url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error en login Firebase, no existe el usuario ingresado: " + conn.getResponseCode());
        }

        String response = new BufferedReader(new InputStreamReader(conn.getInputStream()))
            .lines()
            .collect(Collectors.joining("\n"));


        JSONObject json = new JSONObject(response);

        String token = json.getString("idToken");


        return token;

    }
    
}
