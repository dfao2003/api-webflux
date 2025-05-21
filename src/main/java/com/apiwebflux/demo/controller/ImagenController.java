/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.apiwebflux.demo.controller;

import com.apiwebflux.demo.model.ImagenRequest;
import com.apiwebflux.demo.service.FirebasePycudaAPI;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/procesar")
public class ImagenController {
    
   private final FirebasePycudaAPI service;

    public ImagenController(FirebasePycudaAPI service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> procesar(@RequestBody ImagenRequest request) {
        return service.enviarImagen(request.getBase64())
                .map(resultado -> ResponseEntity.ok().body(resultado));
    }
    
}
