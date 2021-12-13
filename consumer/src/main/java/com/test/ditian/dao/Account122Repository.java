package com.test.ditian.dao;


import com.test.ditian.entity.Account122;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Account122Repository extends JpaRepository<Account122, Long> {

    List<Account122> findAllByName(String name);

}
