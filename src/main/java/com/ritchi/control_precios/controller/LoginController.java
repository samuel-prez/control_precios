package com.ritchi.control_precios.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.faces.view.ViewScoped;

import java.io.Serializable;

@Component("loginMB")
@ViewScoped
public class LoginController implements Serializable {

    public String getUsername() {

        Object datoUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (datoUser instanceof UserDetails) {

            return ((UserDetails) datoUser).getUsername();
        }

        return datoUser.toString();
    }

}