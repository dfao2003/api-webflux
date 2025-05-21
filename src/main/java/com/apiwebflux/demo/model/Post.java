/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.model;

import com.google.cloud.spring.data.firestore.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
/**
 *
 * @author USUARIO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collectionName = "post")
public class Post {
    @Id
    private int id;
    private String description;
    private int idLikes;
    private int idComments;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Document(collectionName = "likes")
    public static class Like{
        @Id
        private int id;
        private int userid;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Document(collectionName = "comments")
    public static class Comment{
        @Id
        private int id;
        private String commentary;
    }

}

