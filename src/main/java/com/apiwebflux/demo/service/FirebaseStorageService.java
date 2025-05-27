/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import java.util.Base64;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author USUARIO
 */
public class FirebaseStorageService {
    public Mono<String> subirImagenBase64(String nombreArchivo, String base64) {
        return Mono.fromCallable(() -> {
            try {
                byte[] imagenBytes = Base64.getDecoder().decode(base64);

                Bucket bucket = StorageClient.getInstance().bucket();

                Blob blob = bucket.create(nombreArchivo, imagenBytes, "image/jpeg");
                blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
                System.out.println(blob.getName());
                System.out.println("Guardado de foto");

                return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), blob.getName());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("Error al subir la imagen a Firebase Storage: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic()); // Ejecuta en un hilo no bloqueante
    }
    
}
