package com.apiwebflux.demo.repository;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.http.ResponseEntity;

import com.apiwebflux.demo.model.User;
import com.google.firebase.auth.FirebaseAuthException;
import reactor.core.publisher.Mono;

public interface IUserRepository {

    Mono<String> signIn(User user);
    Mono<ResponseEntity<String>> modify(String email, String name, String photo);
   
}
