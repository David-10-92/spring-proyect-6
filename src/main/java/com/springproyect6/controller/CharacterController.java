package com.springproyect6.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.function.Supplier;

import com.springproyect6.service.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.springproyect6.service.RickAndMortyApiService;
import com.springproyect6.service.errors.ServiceError;

@Controller
public class CharacterController {

    private <T> ResponseEntity handleRequest(Supplier<T> supplier){
        try{
            return ResponseEntity.ok(supplier.get());
        }catch(ServiceError e){
            return new ResponseEntity(e.getMessage(), HttpStatusCode.valueOf(e.getErrorCode().getHttpErrorCode()));
        }
    }

    @Autowired
    private RickAndMortyApiService rickAndMortyApiService;

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("search", new SearchFormDTO());
        return "search";
    }

    @PostMapping("/results")
    public String searchCharacters(@ModelAttribute SearchFormDTO searchFormDTO, Model model, @RequestParam(defaultValue = "0") int page) throws UnsupportedEncodingException {
        Page<CharacterDTO> characterPage = rickAndMortyApiService.fetchCharacterPage(PageRequest.of(page, searchFormDTO.getPageSize()), searchFormDTO);
        model.addAttribute("results", characterPage.getContent());
        model.addAttribute("totalPages", characterPage.getTotalPages());
        model.addAttribute("currentPage", characterPage.getNumber());
        model.addAttribute("totalElements", characterPage.getTotalElements());
        return "characters";
    }

    @GetMapping("/view-character")
    public String viewFichaByUrl(@RequestParam("url") String url, Model model) {
        CharacterIdDTO characterIdDTO = rickAndMortyApiService.getCharacterDetails(url);
        // Obtener las valoraciones del personaje por su ID
        Long characterId = characterIdDTO.getId();
        List<ValorationDTO> valorations = rickAndMortyApiService.getValorationsByCharacterId(characterId);
        Double averageValoration  = rickAndMortyApiService.getAverageValorationByCharacterId(characterId);
        // Añadir las valoraciones al modelo
        characterIdDTO.setValorationDTOList(valorations);
        characterIdDTO.setMedia(averageValoration );
        model.addAttribute("character", characterIdDTO);
        model.addAttribute("averageValoration", averageValoration != null ? averageValoration : 0.0);
        return "character-profile";
    }

    @GetMapping("/form-rating")
    public String viewForm(@RequestParam("url") String url,Model model) {
        //Convertir la URL de String a long (asumiendo que la URL representa un ID numérico)
        int idCharacter = Integer.parseInt(url);
        model.addAttribute("comment",new CommentDTO());
        model.addAttribute("idCharacter",idCharacter);
        return "assessment"; // Nombre de la plantilla Thymeleaf que contiene el formulario
    }

    @PostMapping("/save-rating")
    public String saveRating(@ModelAttribute CommentDTO commentDTO) {
        // Establece el ID del personaje en el DTO del comentario
        rickAndMortyApiService.createComment(commentDTO);
        return "redirect:/view-character";
    }
}
