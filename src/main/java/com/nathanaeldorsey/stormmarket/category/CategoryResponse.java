package com.nathanaeldorsey.stormmarket.category;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CategoryResponse {

    private Long id;
    private String name;
    private Set<Long> productIds;

}
