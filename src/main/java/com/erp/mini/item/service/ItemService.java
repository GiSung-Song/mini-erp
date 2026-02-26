package com.erp.mini.item.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.dto.AddItemRequest;
import com.erp.mini.item.dto.ChangeItemPriceRequest;
import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import com.erp.mini.item.repo.ItemCodeSequenceRepository;
import com.erp.mini.item.repo.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemCodeSequenceRepository itemCodeSequenceRepository;

    private static final String ITEM_CODE_PREFIX = "IC";

    // 등록
    @Transactional
    public void addItem(AddItemRequest request) {
        Long sequence = itemCodeSequenceRepository.getItemCodeSequence().getNextAndIncrement();
        String code = ITEM_CODE_PREFIX + String.format("%06d", sequence);

        Item item = Item.createItem(
                request.name(), code, request.basePrice(), request.itemStatus()
        );

        itemRepository.save(item);
    }

    // 비활성화
    @Transactional
    public void deactivateItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 상품이 존재하지 않습니다."));

        item.deactivate();
    }

    // 상품 목록 조회(Prefix(인덱스 활용) + like(startsWith))
    @Transactional(readOnly = true)
    public PageResponse<SearchItemResponse> getItemBySearch(SearchItemCondition searchItemCondition, Pageable pageable) {
        Page<SearchItemResponse> contents = itemRepository.search(searchItemCondition, pageable);

        return PageResponse.from(contents);
    }

    // 수정 (가격 변동)
    @Transactional
    public void changePrice(Long itemId, ChangeItemPriceRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 상품이 존재하지 않습니다."));

        item.changePrice(request.basePrice());
    }
}
