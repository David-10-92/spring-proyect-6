package com.springproyect6.service.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchFormDTO {
    private String name;
    private String status;
    private String species;
    private String type;
    private String gender;
    private int pageSize;
    private int page;
}
