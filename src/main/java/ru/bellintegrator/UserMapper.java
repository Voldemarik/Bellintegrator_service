package ru.bellintegrator;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstname", source = "firstname")
    @Mapping(target = "lastname", source = "lastname")
    @Mapping(target = "age", source = "age")
    User toDomainUser(UserEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstname", source = "firstname")
    @Mapping(target = "lastname", source = "lastname")
    @Mapping(target = "age", source = "age")
    UserEntity toUserEntity(User domain);
}