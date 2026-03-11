package com.springbootdeveloper.RepositoryLayer;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import com.springbootdeveloper.Models.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DatabaseLayerUser extends CrudRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email); //auto implemented by SpringBoot as I used the keywords it provides
    
    
}
			