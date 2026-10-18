package com.nathanaeldorsey.stormmarket.category;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {

    private Long id;
    private String name;
    private Set<Long> productIds;
}
