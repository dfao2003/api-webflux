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

import com.apiwebflux.demo.model.Auth;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Repository
public class AuthRepository implements IAuthRepository{

    @Override
    public String login(Auth user) throws MalformedURLException, IOException {

        String FIREBASE_WEB_API_KEY = "AIzaSyDyj7O9dQ6fN7HkVHpybTmzVO-7Q-6DGFE";
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_WEB_API_KEY;

        String requestBody = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", user.email, user.password
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
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

        // Extraer el idToken del JSON (puedes usar una librería como Jackson o Gson aquí)
        JSONObject json = new JSONObject(response);
        String token = json.getString("idToken");
        System.out.println(token);
        return token;
    }
    
}
