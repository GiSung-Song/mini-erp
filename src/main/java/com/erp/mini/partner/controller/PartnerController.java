package com.erp.mini.partner.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.dto.AddPartnerRequest;
import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import com.erp.mini.partner.dto.UpdatePartnerRequest;
import com.erp.mini.partner.service.PartnerService;
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
@RequestMapping("/api/partner")
@Tag(name = "Partner", description = "거래처 API")
public class PartnerController {

    private final PartnerService partnerService;

    @Operation(summary = "거래처 등록", description = "거래처를 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> addPartner(
            @RequestBody @Valid AddPartnerRequest request
    ) {
        partnerService.addPartner(request);

        return CustomResponse.created();
    }

    @Operation(summary = "거래처 정보 수정", description = "거래처 정보를 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공")
    })
    @PatchMapping("/{partnerId}")
    public ResponseEntity<CustomResponse<Void>> addPartner(
            @PathVariable Long partnerId,
            @RequestBody @Valid UpdatePartnerRequest request
    ) {
        partnerService.updatePartner(partnerId, request);

        return CustomResponse.ok();
    }

    @Operation(summary = "거래처 검색", description = "거래처 목록을 검색한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<SearchPartnerResponse>>> searchPartners(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PartnerType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SearchPartnerCondition condition = new SearchPartnerCondition(keyword, type);
        Pageable pageable = PageRequest.of(page, size);

        PageResponse<SearchPartnerResponse> response = partnerService.searchPartners(condition, pageable);

        return CustomResponse.ok(response);
    }
}
