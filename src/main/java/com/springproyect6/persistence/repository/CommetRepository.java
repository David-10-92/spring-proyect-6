package com.springproyect6.persistence.repository;

import com.springproyect6.persistence.entity.Comment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CommetRepository extends CrudRepository<Comment,Long> {
    @Query("select coalesce(avg(c.valoration),0) from Comment c where c.idCharacter = ?1")
    Double getAverageValoration(Integer idCharacter);
}
