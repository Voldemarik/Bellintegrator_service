package ru.bellintegrator.users_service.mapper;

import org.mapstruct.Mapper;
import ru.bellintegrator.users_service.entity.UserEntity;
import ru.bellintegrator.users_service.model.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDomainUser(UserEntity entity);
    UserEntity toUserEntity(UserDto domain);
}