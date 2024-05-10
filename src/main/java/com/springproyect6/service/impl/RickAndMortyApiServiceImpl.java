package com.springproyect6.service.impl;

import com.springproyect6.persistence.entity.Comment;
import com.springproyect6.persistence.repository.CommetRepository;
import com.springproyect6.service.dtos.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.springproyect6.service.RickAndMortyApiService;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
    public Page<CharacterDTO> fetchCharacterPage(Pageable pageable, SearchFormDTO searchFormDTO) throws UnsupportedEncodingException {
        ResponseEntity<String> responseEntity = getPage(pageable,searchFormDTO);
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
            // Manejar la respuesta no exitosa, por ejemplo, lanzar una excepción o devolver una página vacía
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        JSONObject responseObject = new JSONObject(responseEntity.getBody());
        JSONArray resultsArray = responseObject.getJSONArray("results");

        List<CharacterDTO> characters = mapToCharacterDTOList(resultsArray);

        int totalCharacters = responseObject.getJSONObject("info").getInt("count");
        return new PageImpl<>(characters, pageable, totalCharacters);
    }

    private ResponseEntity<String> getPage(Pageable pageable, SearchFormDTO searchFormDTO) throws UnsupportedEncodingException {
        // Obtener el número de página actual
        int page = pageable.getPageNumber();

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
        return restTemplate.getForEntity(url, String.class);
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

    private List<CharacterDTO> mapToCharacterDTOList(JSONArray resultsArray) {
        List<CharacterDTO> characters = new ArrayList<>();
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject characterObject = resultsArray.getJSONObject(i);
            CharacterDTO characterDTO = mapToCharacterDTO(characterObject);
            characters.add(characterDTO);
        }
        return characters;
    }

    private CharacterDTO mapToCharacterDTO(JSONObject characterObject) {
        CharacterDTO characterDTO = new CharacterDTO();
        characterDTO.setName(characterObject.getString("name"));
        characterDTO.setStatus(characterObject.getString("status"));
        characterDTO.setSpecies(characterObject.getString("species"));
        JSONObject origenObject = characterObject.getJSONObject("origin");
        characterDTO.setOrigin(origenObject.getString("name"));
        characterDTO.setUrl(characterObject.getString("url"));

        return characterDTO;
    }


    public CharacterIdDTO getCharacterDetails(String url) {
        // Realizar una solicitud GET a la URL especificada y obtener la respuesta
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        // Convertir la respuesta a un objeto JSON para acceder a los datos
        JSONObject characterObject = new JSONObject(responseEntity.getBody());

        // Inicializar un nuevo objeto CharacterIdDTO para almacenar los detalles del personaje
        CharacterIdDTO characterIdDTO = new CharacterIdDTO();
        // Asignar los valores de los atributos del objeto CharacterIdDTO a partir de los datos del objeto JSON
        characterIdDTO.setId(characterObject.getLong("id"));
        characterIdDTO.setImage(characterObject.getString("image"));
        characterIdDTO.setName(characterObject.getString("name"));
        characterIdDTO.setStatus(characterObject.getString("status"));

        // Verificar si el objeto JSON contiene la clave "types"
        if (characterObject.has("types")) {
            // Si la clave "types" existe, asignar su valor al atributo "types" del objeto CharacterIdDTO
            characterIdDTO.setTypes(characterObject.getString("types"));
        } else {
            // Si la clave "types" no existe, asignar un valor predeterminado o manejar el caso según sea necesario
            characterIdDTO.setTypes("Sin tipo");
        }

        // Asignar los valores de los atributos restantes del objeto CharacterIdDTO
        characterIdDTO.setGender(characterObject.getString("gender"));
        characterIdDTO.setOrigin(characterObject.getJSONObject("origin").getString("name"));
        characterIdDTO.setLocation(characterObject.getJSONObject("location").getString("name"));

        // Obtener la longitud del array "episode" y asignarla al atributo "episodes" del objeto CharacterIdDTO
        JSONArray resultsArray = characterObject.getJSONArray("episode");
        characterIdDTO.setEpisodes(resultsArray.length());

        // Devolver el objeto CharacterIdDTO completo con todos los detalles del personaje
        return characterIdDTO;
    }

    public List<ValorationDTO> getValorationsByCharacterId(Long characterId) {
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

    public Double getAverageValorationByCharacterId(Long characterId) {
        // Consulta SQL para obtener la media de las valoraciones por ID de personaje
        String sql = "SELECT AVG(valoration) AS avg_valoration FROM comentarios WHERE id_character = ?";
        // Ejecutar la consulta y obtener el resultado
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, characterId);
        Object avgValorationObject = result.get("avg_valoration");
        // Obtener la media de las valoraciones
        BigDecimal averageValoration = (BigDecimal) result.get("avg_valoration");
        // Convertir BigDecimal a Double
        return averageValoration != null ? averageValoration.doubleValue() : 0;
    }


    public void createComment(CommentDTO commentDTO){
        Comment comment = new Comment();
        comment.setIdCharacter(commentDTO.getIdCharacter());
        comment.setName(commentDTO.getNameCharacter());
        comment.setComment(commentDTO.getComment());
        comment.setValoration(commentDTO.getValoration());
        commetRepository.save(comment);
    }
}
