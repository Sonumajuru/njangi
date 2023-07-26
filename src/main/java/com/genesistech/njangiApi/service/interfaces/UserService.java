package com.genesistech.njangiApi.service.interfaces;

import com.genesistech.njangiApi.Enum.Status;
import com.genesistech.njangiApi.model.User;
import java.util.List;

/** Interface Class */
public interface UserService {

    /**
     * Add operation
     */
    void SaveUser(User user);

    /** Read operation */
    List<User> getUsers();

    /** Read operation */
    User getUserById(Long userId);

    User getByEmail(String email);

    Boolean existsByEmail(String email);

    /**
     * Update operation
     */
    void updateUser(User user, Long userId);

    /** Delete operation */
    Status deleteUserById(Long userId);
}