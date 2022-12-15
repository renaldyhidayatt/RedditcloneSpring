package com.sanedge.reditclone.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sanedge.reditclone.models.User;
import com.sanedge.reditclone.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username not found"));
    }

}