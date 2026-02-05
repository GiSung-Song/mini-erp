package com.erp.mini.partner.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.dto.AddPartnerRequest;
import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import com.erp.mini.partner.dto.UpdatePartnerRequest;
import com.erp.mini.partner.repo.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class PartnerService {
    private final PartnerRepository partnerRepository;

    @Transactional
    public void addPartner(AddPartnerRequest request) {
        Partner partner = partnerRepository.save(
                Partner.createPartner(
                        request.name(), request.type(), request.phone(), request.email()
                )
        );

        partner.generateCode();
    }

    @Transactional
    public void updatePartner(Long partnerId, UpdatePartnerRequest request) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 거래처를 찾을 수 없습니다."));

        updateNullable(request.email(), partner::changeEmail);
        updateNullable(request.phone(), partner::changePhone);
    }

    @Transactional(readOnly = true)
    public PageResponse<SearchPartnerResponse> searchPartners(SearchPartnerCondition condition, Pageable pageable) {
        Page<SearchPartnerResponse> contents = partnerRepository.search(condition, pageable);

        return PageResponse.from(contents);
    }

    // Consumer<T> Java 8 함수형 인터페이스 (partner::changeEmail, partner::changePhone)
    // 중복 제거 및 같은 규칙 여러 필드에 적용
    private void updateNullable(String value, Consumer<String> updater) {
        if (value == null) return; // 변경 없음
        if (value.isBlank()) updater.accept(null); // 삭제
        else updater.accept(value); // 변경
    }
}
