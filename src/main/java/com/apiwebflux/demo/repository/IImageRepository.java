package com.apiwebflux.demo.repository;

import org.json.JSONObject;

import com.apiwebflux.demo.model.ImagenRequest;
import com.apiwebflux.demo.model.Post;

public interface IImageRepository {
    public String publicPost(Post post);
    public String applyFilter(ImagenRequest request);
}
