package ru.bellintegrator.users_service.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.bellintegrator.users_service.entity.UserEntity;
import ru.bellintegrator.users_service.model.UserFilter;

public class UserSpecification {

    public static Specification<UserEntity> build(UserFilter f) {
        return Specification.allOf(
                firstnameContains(f.getFirstname()),
                lastnameContains(f.getLastname()),
                minAge(f.getMinAge()),
                maxAge(f.getMaxAge())
        );
    }

    private static Specification<UserEntity> firstnameContains(String value) {
        return (root, query, cb) ->
            value == null ? null : cb.like(cb.lower(root.get("firstname")), "%" + value.toLowerCase() + "%");
    }

    private static Specification<UserEntity> lastnameContains(String value) {
        return (root, query, cb) ->
                value == null ? null : cb.like(cb.lower(root.get("lastname")), "%" + value.toLowerCase() + "%");
    }

    private static Specification<UserEntity> minAge(Integer value) {
        return (root, query, cb) ->
                value == null ? null : cb.greaterThanOrEqualTo(root.get("age"), value);
    }

    private static Specification<UserEntity> maxAge(Integer value) {
        return (root, query, cb) ->
                value == null ? null : cb.lessThanOrEqualTo(root.get("age"), value);
    }
}
