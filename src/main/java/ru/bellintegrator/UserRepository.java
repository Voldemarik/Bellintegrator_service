package ru.bellintegrator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository
        extends JpaRepository<UserEntity, UUID> {

    @Query(value = """
        SELECT * FROM users u 
        WHERE (:firstname IS NULL OR u.firstname ILIKE '%' || :firstname || '%') 
          AND (:lastname IS NULL OR u.lastname ILIKE '%' || :lastname || '%') 
          AND (:minAge IS NULL OR u.age >= :minAge) 
          AND (:maxAge IS NULL OR u.age <= :maxAge)
        """,
            countQuery = """
        SELECT COUNT(*) FROM users u 
        WHERE (:firstname IS NULL OR u.firstname ILIKE '%' || :firstname || '%') 
          AND (:lastname IS NULL OR u.lastname ILIKE '%' || :lastname || '%') 
          AND (:minAge IS NULL OR u.age >= :minAge) 
          AND (:maxAge IS NULL OR u.age <= :maxAge)
        """,
            nativeQuery = true)
    Page<UserEntity> findByFilters(
            @Param("firstname") String firstname,
            @Param("lastname") String lastname,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable
    );
}