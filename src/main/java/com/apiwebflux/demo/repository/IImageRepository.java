package com.apiwebflux.demo.repository;

import org.json.JSONObject;

import com.apiwebflux.demo.model.ImagenRequest;
import com.apiwebflux.demo.model.Post;
import reactor.core.publisher.Mono;

public interface IImageRepository {
    public Mono<String> publicPost(Post post);
    public Mono<String> applyFilter(ImagenRequest request);
}
