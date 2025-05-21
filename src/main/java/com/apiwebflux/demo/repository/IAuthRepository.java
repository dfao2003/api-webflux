package com.apiwebflux.demo.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.apiwebflux.demo.model.Auth;
import com.apiwebflux.demo.model.User;

public interface IAuthRepository {
    public String login(Auth user) throws FileNotFoundException, MalformedURLException, IOException;
}
