package com.mycompany._thstudy.auth.command.infrastructure.repository;

import com.mycompany._thstudy.auth.command.domain.aggregate.RefreshToken;
import com.mycompany._thstudy.auth.command.domain.repository.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, String>, RefreshTokenRepository {

    @Override
    Optional<RefreshToken> findByUserEmail(String userEmail);

    @Override
    void deleteByUserEmail(String userEmail);
}
