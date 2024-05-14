package com.springproyect6.service.apimodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PageInfo {
    int count;
    int pages;
    String next;
    String prev;
}
