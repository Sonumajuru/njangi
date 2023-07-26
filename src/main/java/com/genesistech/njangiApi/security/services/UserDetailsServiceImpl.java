package com.genesistech.njangiApi.security.services;

import com.genesistech.njangiApi.repo.UserRepo;
import com.genesistech.njangiApi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepo userService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email);
        if (user == null) {
            return (UserDetails) new UsernameNotFoundException("User Not Found with username: " + email);
        }

        return UserDetailsImpl.build(user);
    }
}