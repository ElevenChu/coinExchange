package com.elevenchu.service;

import com.elevenchu.model.LoginForm;
import com.elevenchu.model.LoginUser;

public interface LoginService {
    LoginUser login(LoginForm loginForm);
}
