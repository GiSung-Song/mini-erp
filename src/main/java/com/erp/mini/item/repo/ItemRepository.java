package com.erp.mini.item.repo;

import com.erp.mini.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByCode(String code);
    boolean  existsByCode(String code);
}