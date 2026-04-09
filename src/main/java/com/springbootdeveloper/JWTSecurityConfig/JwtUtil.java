package com.springbootdeveloper.JWTSecurityConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String SECRET =
            "mysecretkeymysecretkeymysecretkeymysecretkey";

    // Retrieve username from JWT token
    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    // Retrieve expiration date from JWT token
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    // Retrieve all claims from token
    private Claims getAllClaimsFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims;
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        Date currentDate = new Date();

        if (expiration.before(currentDate)) {
            return true;
        } else {
            return false;
        }
    }

    // Generate JWT token
    public String generateToken(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        UserDetails user;

        if (principal instanceof UserDetails) {
            user = (UserDetails) principal;
        } else {
            throw new RuntimeException("Principal is not of type UserDetails");
        }

        Map<String, Object> claims = new HashMap<String, Object>();

        String token = Jwts.builder()
                .setClaims(claims)
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

        return token;
    }

    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {

        String usernameFromToken = getUsernameFromToken(token);
        String usernameFromUserDetails = userDetails.getUsername();

        Boolean isExpired = isTokenExpired(token);

        if (usernameFromToken.equals(usernameFromUserDetails) && !isExpired) {
            return true;
        } else {
            return false;
        }
    }
}