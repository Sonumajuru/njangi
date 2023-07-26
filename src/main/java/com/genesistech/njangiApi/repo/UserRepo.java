package com.genesistech.njangiApi.repo;

import com.genesistech.njangiApi.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/* Interface extending CrudRepository */
public interface UserRepo extends CrudRepository<User, Long> {
    User findByEmail(String email);
    Boolean existsByEmail(String email);
}