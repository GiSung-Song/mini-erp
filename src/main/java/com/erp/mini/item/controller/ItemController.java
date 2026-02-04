package com.erp.mini.item.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.item.dto.*;
import com.erp.mini.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/item")
@Tag(name = "Item", description = "상품 API")
public class ItemController {

    private final ItemService itemService;

    // 등록
    @Operation(summary = "상품 등록", description = "상품을 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> addItem(@Valid @RequestBody AddItemRequest request) {
        itemService.addItem(request);

        return CustomResponse.created();
    }

    // 비활성화 (soft delete)
    @Operation(summary = "상품 비활성화", description = "상품을 비활성화 상태로 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<CustomResponse<Void>> deactivateItem(@PathVariable Long itemId) {
        itemService.deactivateItem(itemId);

        return CustomResponse.ok();
    }

    // 상품 목록 검색
    @Operation(summary = "상품 목록 검색", description = "상품 목록을 검색한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<SearchItemResponse>>> searchItems(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        SearchItemCondition searchItemCondition = new SearchItemCondition(code, name);

        PageResponse<SearchItemResponse> response = itemService.getItemBySearch(searchItemCondition, pageable);

        return CustomResponse.ok(response);
    }

    // 상품 상세 조회
    // TODO: 추후에 입고, 출고, 재고 등 도메인 로직 완성되면 테스트 예정
    @Operation(summary = "상품 상세 조회", description = "상품을 상세 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{itemId}")
    public ResponseEntity<CustomResponse<ItemDetailResponse>> getItemDetail(@PathVariable Long itemId) {
        ItemDetailResponse response = itemService.getItemDetail(itemId);

        return CustomResponse.ok(response);
    }

    // 수정 (가격)
    @Operation(summary = "상품 가격 수정", description = "상품 가격을 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공")
    })
    @PatchMapping("/{itemId}")
    public ResponseEntity<CustomResponse<Void>> changeItemPrice(
            @PathVariable Long itemId,
            @RequestBody @Valid ChangeItemPriceRequest request
    ) {
        itemService.changePrice(itemId, request);

        return CustomResponse.ok();
    }

}
