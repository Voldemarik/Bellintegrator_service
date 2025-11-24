package ru.bellintegrator.users_service.model;

import lombok.*;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
//DTO используется для создания фильтра
public class UserFilter {
    @Nullable
    private String firstname;
    @Nullable
    private String lastname;
    @Nullable
    private Integer minAge;
    @Nullable
    private Integer maxAge;
    @NonNull
    private Integer page = 0;
    @NonNull
    private Integer size = 5;
}
