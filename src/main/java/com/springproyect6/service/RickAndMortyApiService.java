package com.springproyect6.service;

import java.io.UnsupportedEncodingException;
import com.springproyect6.service.dtos.CharacterDTO;
import com.springproyect6.service.dtos.CharacterIdDTO;
import com.springproyect6.service.dtos.SearchFormDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RickAndMortyApiService {

    Page<CharacterDTO> fetchCharacterPage(Pageable pageable, SearchFormDTO searchFormDTO)throws UnsupportedEncodingException;
    CharacterIdDTO getCharacterDetails(String url);
}