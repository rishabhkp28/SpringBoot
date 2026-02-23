package com.springbootdeveloper.RepositoryLayer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.springbootdeveloper.Models.User;


@Repository
public interface DatabaseLayer extends CrudRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email); //auto implemented by SpringBoot as I used the keywords it provides
}
