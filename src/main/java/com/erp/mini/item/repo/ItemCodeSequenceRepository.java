package com.erp.mini.item.repo;

import com.erp.mini.item.domain.ItemCodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ItemCodeSequenceRepository extends JpaRepository<ItemCodeSequence, Byte> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ItemCodeSequence s where s.id = 1")
    ItemCodeSequence getItemCodeSequence();
}