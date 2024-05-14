package com.springproyect6.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.function.Supplier;
import com.springproyect6.service.dtos.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.springproyect6.service.RickAndMortyApiService;
import com.springproyect6.service.errors.ServiceError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String searchCharacters(@ModelAttribute SearchFormDTO searchFormDTO, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        // Guardar los parámetros de búsqueda en la sesión
        session.setAttribute("searchForm", searchFormDTO);

        // Redirigir a la página de resultados con la paginación iniciada desde la página 0
        redirectAttributes.addAttribute("page", 0);
        return "redirect:/results"; // Redirige a la ruta GET para la paginación y la búsqueda
    }

    @GetMapping("/results")
    public String searchCharactersWithPagination(Model model, HttpSession session,
                                                 @RequestParam(defaultValue = "0") int page) throws UnsupportedEncodingException {
        // Obtener los parámetros de búsqueda de la sesión
        SearchFormDTO storedSearchFormDTO = (SearchFormDTO) session.getAttribute("searchForm");

        // Si no hay ningún parámetro de búsqueda almacenado, redirigir a la página de búsqueda
        if (storedSearchFormDTO == null) {
            return "redirect:/search"; // Esta es una ruta hipotética para la página de búsqueda inicial
        }

        // Obtener el número de resultados por página del objeto SearchFormDTO
        int resultsPerPage = storedSearchFormDTO.getPageSize();

        // Obtener los resultados de la página actual con los filtros de búsqueda
        Page<CharacterDTO> characterPage = rickAndMortyApiService.fetchCharacterPage(PageRequest.of(page, resultsPerPage), storedSearchFormDTO);

        // Agregar los resultados y la información de paginación al modelo
        model.addAttribute("results", characterPage.getContent());
        model.addAttribute("totalPages", characterPage.getTotalPages());
        model.addAttribute("currentPage", characterPage.getNumber());
        model.addAttribute("totalElements", characterPage.getTotalElements());

        return "characters";
    }

    @GetMapping("/view-character")
    public String viewFichaByUrl(@RequestParam("url") String url, Model model) {
        CharacterIdDTO characterIdDTO = rickAndMortyApiService.getCharacterDetails(url);
        characterIdDTO.setUrl(url);
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
        String numberUrl = rickAndMortyApiService.getLastNumbersFromUrl(url);
        int idCharacter = Integer.parseInt(numberUrl);
        model.addAttribute("characterUrl", url);
        model.addAttribute("comment",new CommentDTO());
        model.addAttribute("idCharacter",idCharacter);
        return "assessment"; // Nombre de la plantilla Thymeleaf que contiene el formulario
    }

    @PostMapping("/save-rating")
    public String saveRating(@ModelAttribute CommentDTO commentDTO, @RequestParam("characterUrl") String characterUrl) {
        // Establece el ID del personaje en el DTO del comentario
        rickAndMortyApiService.createComment(commentDTO);
        return "redirect:/view-character?url=" + characterUrl;
    }
}
