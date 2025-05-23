/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.controller;

import com.apiwebflux.demo.model.ImagenRequest;
import com.apiwebflux.demo.model.Post;
import com.apiwebflux.demo.repository.IImageRepository;
import com.apiwebflux.demo.service.FirebasePycudaAPI;

import java.util.HashMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


/**
 *
 * @author USUARIO
 */
@RestController
@RequestMapping("/img")
public class ImagenController {
    
    @Autowired
    private IImageRepository repository;

    @PostMapping("/post")
    public Mono<ResponseEntity<String>> post(@RequestBody Post request) {
        try{
            System.out.println("Realizando publicacion");
            String valor = repository.publicPost(request);
            System.out.println("PUBLICACIÓN HECHA");

            return Mono.just(ResponseEntity.ok("True"));
        }catch(Exception  e){
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()));
        }
    }

    @PostMapping("/filter")
    public Mono<ResponseEntity<HashMap<String, String>>> filter(@RequestBody ImagenRequest req){
        try{
            //System.out.println("Entrando");
            String filtro = repository.applyFilter(req);

            //System.out.println(filtro);
            HashMap<String, String> map = new HashMap<>();
            map.put("imagen", filtro);

            return Mono.just(ResponseEntity.ok(map));

        }catch(Exception e){
            HashMap<String, String> error = new HashMap<>();
            error.put("imagen", e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
        }
    }
    
}
