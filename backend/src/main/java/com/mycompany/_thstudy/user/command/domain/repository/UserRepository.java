package com.mycompany._thstudy.user.command.domain.repository;

import com.mycompany._thstudy.user.command.domain.aggregate.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
