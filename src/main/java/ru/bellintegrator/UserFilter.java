package ru.bellintegrator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilter {

    private String firstname;
    private String lastname;
    private Integer minAge;
    private Integer maxAge;
    private Integer page = 0;
    private Integer size = 5;

    public UserFilter() {}

    public UserFilter(String firstname, String lastname, Integer minAge, Integer maxAge, Integer page, Integer size) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.page = page;
        this.size = size;
    }
}