package com.djiguiya.transport.modules.identity.repository;

import com.djiguiya.transport.modules.identity.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

	Optional<RefreshTokenEntity> findByTokenHashAndRevokedFalse(String tokenHash);
}
