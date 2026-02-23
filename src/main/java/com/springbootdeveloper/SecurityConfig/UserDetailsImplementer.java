package com.springbootdeveloper.SecurityConfig;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.springbootdeveloper.Models.User;
import java.util.List;




public class UserDetailsImplementer implements UserDetails{
	
	private final User user;
	public UserDetailsImplementer(User user)
	{
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {//this extracts the authorities of this User based on user , from security config
		// TODO Auto-generated method stub
		
		SimpleGrantedAuthority sga = new SimpleGrantedAuthority(user.getRole());
		return List.of(sga);
	}

	@Override
	public @Nullable String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

}
