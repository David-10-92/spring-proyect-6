package com.springproyect6.service.impl;

import com.springproyect6.persistence.apimodel.Character;
import com.springproyect6.persistence.apimodel.CharacterList;
import com.springproyect6.persistence.entity.Comment;
import com.springproyect6.persistence.repository.CommetRepository;
import com.springproyect6.service.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.springproyect6.service.RickAndMortyApiService;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RickAndMortyApiServiceImpl implements RickAndMortyApiService{

    //Url base de la api
    private static final String API_URL = "https://rickandmortyapi.com/api/character/";

    //RestTemplate es una clase de Spring que nos permite realizar solicitudes HTTP
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    CommetRepository commetRepository;

    @Override
    public CharacterList fetchCharacterPage(SearchFormDTO searchFormDTO) throws UnsupportedEncodingException {
        return getPage(searchFormDTO);
    }

    private CharacterList getPage(SearchFormDTO searchFormDTO) throws UnsupportedEncodingException {
        // Obtener el número de página actual
        int page = searchFormDTO.getPage();
        // Inicializar un StringBuilder para construir la URL
        StringBuilder urlBuilder = new StringBuilder(API_URL).append("?page=").append(page + 1);
        // Agregar parámetros de búsqueda si existen en el SearchFormDTO
        addQueryParam(urlBuilder, "name", searchFormDTO.getName());
        addQueryParam(urlBuilder, "status", searchFormDTO.getStatus());
        addQueryParam(urlBuilder, "species", searchFormDTO.getSpecies());
        addQueryParam(urlBuilder, "type", searchFormDTO.getType());
        addQueryParam(urlBuilder, "gender", searchFormDTO.getGender());
        // Convertir el StringBuilder a String representando la URL final
        String url = urlBuilder.toString();
        // Realizar una solicitud GET a la URL y devolver el ResponseEntity
        return restTemplate.getForEntity(url, CharacterList.class).getBody();
    }

    // Método para agregar un parámetro de consulta a la URL si el valor del parámetro no es nulo o vacío
    private void addQueryParam(StringBuilder urlBuilder, String paramName, String paramValue) throws UnsupportedEncodingException {
        if (paramValue != null && !paramValue.isEmpty()) {
            // Agregar el parámetro a la URL
            urlBuilder.append("&").append(paramName).append("=").append(encodeValue(paramValue));
        }
    }

    // Método para codificar en URL un valor de parámetro
    private String encodeValue(String value) throws UnsupportedEncodingException {
        // Usar URLEncoder para codificar el valor del parámetro
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    public Character getCharacterDetails(Integer id) {
        String url = String.format("%s/%d",API_URL,id); // Esto simplemente concatena y reemplaza
        ResponseEntity<Character> response = restTemplate.getForEntity(url, Character.class); // Faltaría la validación de excepciones
        return response.getBody();
    }

    public List<ValorationDTO> getValorationsByCharacterId(Integer characterId) {
        // Consulta SQL para obtener las valoraciones por ID de personaje
        String sql = "SELECT name, valoration, comment FROM comentarios WHERE id_character = ?";

        try {
            // Ejecutar la consulta y obtener los resultados
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, characterId);

            // Inicializar una lista para almacenar las valoraciones encontradas
            List<ValorationDTO> valorations = new ArrayList<>();

            // Iterar sobre los resultados y crear objetos ValorationDTO
            for (Map<String, Object> row : rows) {
                ValorationDTO valorationDTO = new ValorationDTO();
                valorationDTO.setName((String) row.get("name"));
                valorationDTO.setValoration((int) row.get("valoration"));
                valorationDTO.setComment((String) row.get("comment"));
                valorations.add(valorationDTO);
            }

            // Devolver la lista de valoraciones
            return valorations;
        } catch (EmptyResultDataAccessException e) {
            // Manejar el caso en que no hay valoraciones para el personaje
            return new ArrayList<>();
        }
    }

    public Double getAverageValorationByCharacterId(Integer characterId) {
        return commetRepository.getAverageValoration(characterId);
    }

    public void createComment(CommentDTO commentDTO){
        Comment comment = new Comment();
        comment.setIdCharacter(commentDTO.getIdCharacter());
        comment.setName(commentDTO.getNameCharacter());
        comment.setComment(commentDTO.getComment());
        comment.setValoration(commentDTO.getValoration());
        commetRepository.save(comment);
    }

    public  String getLastNumbersFromUrl(String url) {
        // Divide la URL en partes usando "/" como separador
        String[] partes = url.split("/");

        // Selecciona el último elemento de la matriz resultante
        String ultimoNumero = partes[partes.length - 1];

        return ultimoNumero;
    }

}
