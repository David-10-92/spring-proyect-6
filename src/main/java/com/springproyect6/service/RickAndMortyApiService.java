package com.springproyect6.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.springproyect6.service.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RickAndMortyApiService {

    Page<CharacterDTO> fetchCharacterPage(Pageable pageable, SearchFormDTO searchFormDTO)throws UnsupportedEncodingException;
    CharacterIdDTO getCharacterDetails(String url);
    void createComment(CommentDTO commentDTO);
    List<ValorationDTO> getValorationsByCharacterId(Long characterId);
    Double getAverageValorationByCharacterId(Long characterId);
    String getLastNumbersFromUrl(String urlString);
}
