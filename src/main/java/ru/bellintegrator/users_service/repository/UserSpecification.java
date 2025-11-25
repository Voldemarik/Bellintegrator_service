package ru.bellintegrator.users_service.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.bellintegrator.users_service.entity.UserEntity;

public class UserSpecification {

    public static Specification<UserEntity> nameContains(String field, String value) {
        return (root, query, cb) ->
            value == null ? null : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static Specification<UserEntity> ageContains(Integer age1, Integer age2) {
        return (root, query, cb) -> {
            if (age1 != null && age2 != null) {
                return (age1 <= age2) ? cb.between(root.get("age"), age1, age2) : null;
            } else if (age1 == null && age2 != null) {
                return cb.lessThanOrEqualTo(root.get("age"), age2);
            } else if (age1 != null){
                return cb.greaterThanOrEqualTo(root.get("age"), age1);
            } else {
                return null;
            }
        };
    }
}
