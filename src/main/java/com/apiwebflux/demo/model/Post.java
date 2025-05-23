/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.model;

import java.util.Date;

import com.google.cloud.Timestamp;
import com.google.cloud.spring.data.firestore.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


/**
 *
 * @author USUARIO
 */

//java.util.Date

@Data
public class Post {
    
    //public String url;
    public String photo;
    public String name;
    public String uid_user;
    public String description;
    public Date created_at;


}

