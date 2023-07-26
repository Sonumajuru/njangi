package com.genesistech.njangiApi.repo;

import com.genesistech.njangiApi.Enum.ERole;
import com.genesistech.njangiApi.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/* Interface extending CrudRepository */
public interface RoleRepo extends CrudRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}