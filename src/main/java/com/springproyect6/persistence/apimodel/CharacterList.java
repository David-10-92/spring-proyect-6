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
public class CharacterList {
    PageInfo info;
    List<Character> results;
}