package com.springproyect6.persistence.repository;

import com.springproyect6.persistence.entity.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommetRepository extends CrudRepository<Comment,Long> {
}
