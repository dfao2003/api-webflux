package com.apiwebflux.demo.repository;

import java.io.IOException;
import java.net.MalformedURLException;

import com.apiwebflux.demo.model.User;
import com.google.firebase.auth.FirebaseAuthException;
import reactor.core.publisher.Mono;

public interface IUserRepository {

    Mono<String> signIn(User user);
   
}
