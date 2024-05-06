package com.springproyect6.service.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class CharacterIdDTO {
    private String image;
    private Long id;
    private String name;
    private String status;
    private String species;
    private String types;
    private String gender;
    private String origin;
    private String location;
    private int episodes;
}
