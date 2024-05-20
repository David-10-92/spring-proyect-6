package com.springproyect6.persistence.apimodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Character {
    Integer id;
    String name;
    String status;
    String species;
    String type;
    String gender;
    Location origin;
    Location location;
    String image;
    List<String> episode;
    String url;
    String created;
}
