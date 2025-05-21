/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.spring.data.firestore.Document;
import org.springframework.data.annotation.Id;

/**
 *
 * @author USUARIO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collectionName = "user")
public class User {
    
    @Id
    private int uid;
    private String nombreUsuario;
    private String email;
    private String password;
    
}
