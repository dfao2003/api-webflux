package com.apiwebflux.demo.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.apiwebflux.demo.model.Auth;
import com.apiwebflux.demo.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAuthRepository {
    Mono<String> login(Auth user);
}
