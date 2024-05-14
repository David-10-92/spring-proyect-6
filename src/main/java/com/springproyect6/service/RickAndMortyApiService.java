package com.springproyect6.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.springproyect6.service.apimodel.Character;
import com.springproyect6.service.apimodel.CharacterList;
import com.springproyect6.service.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RickAndMortyApiService {

    CharacterList fetchCharacterPage(SearchFormDTO searchFormDTO) throws UnsupportedEncodingException;
    Character getCharacterDetails(Integer id);
    void createComment(CommentDTO commentDTO);
    List<ValorationDTO> getValorationsByCharacterId(Integer characterId);
    Double getAverageValorationByCharacterId(Integer characterId);
    String getLastNumbersFromUrl(String urlString);
}
