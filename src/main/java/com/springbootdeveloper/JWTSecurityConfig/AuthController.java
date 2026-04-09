package com.springbootdeveloper.JWTSecurityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springbootdeveloper.DTO.UserLoginDto;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("UserLog") UserLoginDto userLog,
            BindingResult result,
            HttpServletResponse response) {

        if (result.hasErrors()) {
            return "login";
        }

        try {
            Authentication authentication =
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        userLog.getEmail(),
                        userLog.getPassword() //throws exception if something is incorrect
                    )
                );

            String token = jwtUtil.generateToken(authentication);

            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            return "redirect:/user/dashboard";

        } catch (BadCredentialsException ex) {

            // ✅ GLOBAL error (not tied to any field)
            result.reject("login.failed", "Invalid email or password");

            return "login";
        }
    }
}