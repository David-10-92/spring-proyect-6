package com.springproyect6.controller;

import java.io.UnsupportedEncodingException;
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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RickAndMortyApiService rickAndMortyApiService;

    @GetMapping("/search")
    public String searchHtml(Model model) {
        model.addAttribute("searchFormDTO",new SearchFormDTO());
        return "search";
    }

    @PostMapping(path ="/search",consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String searchCharacters(@ModelAttribute SearchFormDTO searchFormDTO, Model model,@RequestParam(defaultValue = "0") int page) throws UnsupportedEncodingException {
        Page<CharacterDTO> characterPage = rickAndMortyApiService.fetchCharacterPage(PageRequest.of(page,searchFormDTO.getPageSize()), searchFormDTO);
        model.addAttribute("results",characterPage.getContent());
        model.addAttribute("totalPages", characterPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", characterPage.getTotalElements());
        return "characters";
    }

    @GetMapping("/view-character")
    public String verFicha(@RequestParam("url") String url, Model model) {
        CharacterIdDTO characterIdDTO = rickAndMortyApiService.getCharacterDetails(url);
        model.addAttribute("character", characterIdDTO);
        return "character-profile";
    }

    @GetMapping("/form-rating")
    public String mostrarFormulario(Model model) {
        model.addAttribute("comment",new CommentDTO());
        return "assessment"; // Nombre de la plantilla Thymeleaf que contiene el formulario
    }

    @PostMapping("/save-rating")
    public String saveRating(@ModelAttribute CommentDTO commentDTO) {
        rickAndMortyApiService.createComment(commentDTO);
        return "redirect:/character-profile";
    }
}
