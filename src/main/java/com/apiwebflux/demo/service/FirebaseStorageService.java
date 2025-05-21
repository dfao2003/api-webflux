/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import java.util.Base64;

/**
 *
 * @author USUARIO
 */
public class FirebaseStorageService {
    public String subirImagenBase64(String nombreArchivo, String base64) throws IOException{
        
        byte[] imagenBytes = Base64.getDecoder().decode(base64.split(",")[1]);
        
        Bucket bucket = StorageClient.getInstance().bucket();
        
        Blob blob = bucket.create(nombreArchivo, imagenBytes, "imagen/png");
        
        return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), blob.getName());
    }
}
