package com.apiwebflux.demo.repository;

import java.io.IOException;
import java.net.MalformedURLException;

import com.apiwebflux.demo.model.User;
import com.google.firebase.auth.FirebaseAuthException;

public interface IUserRepository {

    public String signIn(User user) throws FirebaseAuthException, MalformedURLException, IOException;
    

}
