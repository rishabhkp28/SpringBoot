package com.springbootdeveloper.JWTSecurityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.springbootdeveloper.SecurityConfig.UserDetailsServiceImplementer;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;


import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImplementer userDetailsService;

    // ✅ new method - checks both header and cookie
    private String extractToken(HttpServletRequest request) {

        // 1. check Authorization header first (for future API calls)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 2. check cookie (for Thymeleaf browser navigation)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("jwt")) {
                    return cookie.getValue();
                }
            }
        }

        return null; // no token found anywhere
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException 
    {

        // ✅ use extractToken instead of reading header directly
        String token = extractToken(request);
        String username = "";
        if (token != null) 
        {
        	
        	try {
        	    username = this.jwtUtil.getUsernameFromToken(token);

		        } 		
        	catch (IllegalArgumentException e)
        	{
		       logger.warn("Unable to get JWT Token", e);
        	} 
        	catch (ExpiredJwtException e) 
        	{
		        	 logger.warn("JWT Token has expired", e);
        	} 
        	catch (MalformedJwtException | SignatureException e) 
        	{
		        logger.warn("Invalid JWT Token", e);
		     } 
        	catch (Exception e) 
        	{
		         logger.error("Unexpected error while processing JWT", e);
        	}
        }
        else
        {
        	logger.info("Invalid Header Value !! ");
        }
        		
  
            if (username != null &&  SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth =    new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            else
            {
            	logger.info("Validation Failed");
            }

        filterChain.doFilter(request, response); // always continue chain as this is run before anything ..
    }
    
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) { // as everything goes through the filter , we will exempt this
        String path = request.getServletPath();
        return path.startsWith("/css/")
            || path.startsWith("/js/")
            || path.startsWith("/images/")
            || path.startsWith("/dynamic/validate/")
            || path.equals("/login")
            || path.equals("/signUp")
            || path.equals("/getStarted");
            
    }
    

  
    
    
}