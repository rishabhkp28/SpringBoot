package com.springbootdeveloper.SecurityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.springbootdeveloper.JWTSecurityConfig.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration{//these class provide the  bean dependencies that spring security needs 
	
	
	private UserDetailsServiceImplementer udsImplementerObject;
	private JwtAuthenticationFilter jwtFilter;	
	
	public SecurityConfiguration(UserDetailsServiceImplementer udsImplementerObject, JwtAuthenticationFilter jwtFilter)// gets injected by itself
	{
		this.udsImplementerObject = udsImplementerObject;
		this.jwtFilter = jwtFilter;
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() //defines how the authentication should take please
	{
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(this.udsImplementerObject);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
	  
	@Bean
	public AuthenticationManager authenticationManager( //provides authentication after loading User from UserDetailsService and comparing encoded pass
	        AuthenticationConfiguration config) throws Exception {
	    return config.getAuthenticationManager();
	}

	/* Used when we wanted to have stateful server system , here storage of credentials is in session
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		//Spring Security deals with authentication , SignUp and all will be handled by us
		
		
		 
		 http.authorizeHttpRequests(auth -> auth
			        .requestMatchers("/signUp", "/login", "/css/**", "/js/**","/getStarted","/dynamic/validate/**").permitAll()//first general matchers then role based then public
			        .requestMatchers("/admin/**").hasAuthority("ADMIN")
			        .requestMatchers("/user/**").hasAuthority("USER") //this role method is predefined spring security paramter that defines hierarchy
			        .anyRequest().authenticated()
			        // hasRole internally checks for ROLE_USER or ROLE_ADMIN while has Authority doesnt
			)
			.formLogin(form -> form
			        .loginPage("/login")//Spring security already handling the security on authentication page
			        .usernameParameter("email")          // IMPORTANT if you're using email field
			        .passwordParameter("password")       // optional if default
			        .defaultSuccessUrl("/user/dashboard", true)// false-> go to the originally requested Url and not dashboard if user requetsed that earlier and reached on login page after redirect
			        .failureUrl("/login?error=true") //format ?key1=value1&key2=value2
			        .permitAll() //this allows the authenticated user to switch to these mentioned without logging in again
			)
			.logout(logout -> logout
			        .logoutUrl("/logout")                     // endpoint to trigger logout
			        .logoutSuccessUrl("/login?logout")        // where to go after logout
			        .invalidateHttpSession(true)              // destroy session
			        .clearAuthentication(true)                // remove auth
			        .deleteCookies("JSESSIONID")              // remove session cookie
			        .permitAll()//this allows the authenticated user to switch to these mentioned without logging in again
			    ).csrf(csrf -> csrf.disable());

	    return http.build();
	    
	   
    }
    */
	// When we want to have a stateless server system, with help of jwt we dont need to store things on server
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		//Spring Security deals with authentication , SignUp and all will be handled by us
		
		
		 
		http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/signUp", "/login", "/css/**", "/js/**","/getStarted","/dynamic/validate/**").permitAll()
            .requestMatchers("/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/user/**").hasAuthority("USER")
            .anyRequest().authenticated()
        );

        http.addFilterBefore(
            jwtFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
	    
	   
    }
	
	

}
