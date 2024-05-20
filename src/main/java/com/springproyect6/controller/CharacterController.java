package com.springproyect6.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.springproyect6.persistence.apimodel.Character;
import com.springproyect6.persistence.apimodel.CharacterList;
import com.springproyect6.service.dtos.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.springproyect6.service.RickAndMortyApiService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CharacterController {

    @Autowired
    private RickAndMortyApiService rickAndMortyApiService;

    @GetMapping("/search")
    public String showSearchForm() {
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

        storedSearchFormDTO.setPage(page);  // Actualizamos el modelo en memoria con la nueva página

        CharacterList list = rickAndMortyApiService.fetchCharacterPage(storedSearchFormDTO);

        // Agregar los resultados y la información de paginación al modelo
        model.addAttribute("results", list.getResults());
        model.addAttribute("totalPages", list.getInfo().getPages());
        model.addAttribute("currentPage", storedSearchFormDTO.getPage());
        model.addAttribute("totalElements", list.getInfo().getCount());

        return "characters";
    }

    @GetMapping("/view-character")
    public String viewFichaByUrl(@RequestParam Integer id, Model model) {
        Character character = rickAndMortyApiService.getCharacterDetails(id);
        // Obtener las valoraciones del personaje por su ID
        List<ValorationDTO> valorations = rickAndMortyApiService.getValorationsByCharacterId(id);
        Double averageValoration  = rickAndMortyApiService.getAverageValorationByCharacterId(id);
        // Añadir las valoraciones al modelo
        model.addAttribute("character", character);
        model.addAttribute("averageValoration",averageValoration);
        model.addAttribute("valorations",valorations);
        return "character-profile";
    }

    @GetMapping("/form-rating")
    public String viewForm(@RequestParam Integer id,Model model) {
        CommentDTO comment = new CommentDTO();
        comment.setIdCharacter(id);
        model.addAttribute("comment",comment);
        return "assessment"; // Nombre de la plantilla Thymeleaf que contiene el formulario
    }

    @PostMapping("/save-rating")
    public String saveRating(@ModelAttribute CommentDTO commentDTO) {
        // Establece el ID del personaje en el DTO del comentario
        rickAndMortyApiService.createComment(commentDTO);
        return "redirect:/view-character?id=" + commentDTO.getIdCharacter();
    }
}
