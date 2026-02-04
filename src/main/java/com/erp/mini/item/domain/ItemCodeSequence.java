package com.erp.mini.item.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_code_sequence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCodeSequence {

    @Id
    private Byte id;

    @Column(nullable = false)
    private Long nextVal;

    public Long getNextAndIncrement() {
        long current = nextVal;
        nextVal = current + 1;
        return current;
    }
}
