package com.springbootdeveloper.SecurityConfig;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.springbootdeveloper.Models.User;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerUser;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;



@Service
public class UserDetailsServiceImplementer implements UserDetailsService {
	
	private final DatabaseLayerUser dl;
	
	public UserDetailsServiceImplementer(DatabaseLayerUser dl)
	{
		this.dl = dl;
	}
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// fetching from database
	
		
		Optional<User> user = dl.findByEmail(email);
	
		if(user.isEmpty())
			throw new UsernameNotFoundException("Could not find the user in the database");
		
		UserDetailsImplementer udi = new UserDetailsImplementer(user.get());
		
		return udi;
	}

}
