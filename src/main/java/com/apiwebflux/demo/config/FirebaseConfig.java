/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;

/**
 *
 * @author USUARIO
 */

@Configuration
public class FirebaseConfig {
    
   @PostConstruct
    public void init() throws IOException {
        try {
            String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (path == null || path.isEmpty()) {
                throw new FileNotFoundException("La variable de entorno GOOGLE_APPLICATION_CREDENTIALS no está definida");
            }

            FileInputStream serviceAccount = new FileInputStream(path);

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("upsglam2.firebasestorage.app")
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo: " + e.getMessage());
        }
    }

    
}
