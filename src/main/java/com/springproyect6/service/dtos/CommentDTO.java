package com.springproyect6.service.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentDTO {
    private Long idChatacter;
    private String nameCharacter;
    private String comment;
    private int valoration;
    private String url;
}
