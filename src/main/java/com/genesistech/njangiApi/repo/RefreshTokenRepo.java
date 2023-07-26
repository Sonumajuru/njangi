package com.genesistech.njangiApi.repo;

import com.genesistech.njangiApi.model.RefreshToken;
import com.genesistech.njangiApi.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/* Interface extending CrudRepository */
public interface RefreshTokenRepo extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Modifying
    int deleteByUser(User user);
}