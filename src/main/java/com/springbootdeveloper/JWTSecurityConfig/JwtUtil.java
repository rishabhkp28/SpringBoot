package com.springbootdeveloper.JWTSecurityConfig;

import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtUtil {

    private static final String SECRET =
        "mysecretkeymysecretkeymysecretkeymysecretkey";

    public String generateToken(Authentication authentication) {

        UserDetails user =
            (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                    new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(
                    Keys.hmacShaKeyFor(SECRET.getBytes()),
                    SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String extractUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}