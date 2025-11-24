package ru.bellintegrator.users_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.bellintegrator.users_service.entity.UserEntity;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
}