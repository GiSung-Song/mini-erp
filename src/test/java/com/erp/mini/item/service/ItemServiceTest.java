package com.erp.mini.item.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemCodeSequence;
import com.erp.mini.item.domain.ItemFixture;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.dto.AddItemRequest;
import com.erp.mini.item.dto.ChangeItemPriceRequest;
import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import com.erp.mini.item.repo.ItemCodeSequenceRepository;
import com.erp.mini.item.repo.ItemRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemCodeSequenceRepository itemCodeSequenceRepository;

    @InjectMocks
    private ItemService itemService;

    @Nested
    class add_item_test {
        @Test
        void add_item_success() {
            AddItemRequest request = new AddItemRequest(
                    "테스트 상품", BigDecimal.valueOf(15000.00), ItemStatus.ACTIVE
            );

            ItemCodeSequence sequence = Mockito.mock(ItemCodeSequence.class);
            given(sequence.getNextAndIncrement()).willReturn(1L);
            given(itemCodeSequenceRepository.getItemCodeSequence()).willReturn(sequence);

            itemService.addItem(request);

            then(itemCodeSequenceRepository).should().getItemCodeSequence();
            then(itemRepository).should().save(argThat(item ->
                    item.getName().equals(request.name())
                            && item.getCode().equals("IC000001")
                            && item.getBasePrice().equals(BigDecimal.valueOf(15000.00))
                            && item.getStatus() == ItemStatus.ACTIVE));
        }
    }

    @Nested
    class item_deactivate_test {
        @Test
        void item_deactivate_success() {
            Item item = ItemFixture.create();

            given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));

            itemService.deactivateItem(item.getId());

            then(itemRepository).should().findById(item.getId());
            assertThat(item.getStatus()).isEqualTo(ItemStatus.INACTIVE);
        }

        @Test
        void item_deactivate_fail_with_not_found() {
            given(itemRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.deactivateItem(anyLong()))
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class search_item_list_test {
        @Test
        void search_item_list_success() {
            Pageable pageable = PageRequest.of(0, 10);

            List<SearchItemResponse> contents =
                    List.of(
                            new SearchItemResponse(1L, "ITEM - 1", "IC000001", ItemStatus.ACTIVE),
                            new SearchItemResponse(2L, "ITEM - 2", "IC000002", ItemStatus.ACTIVE),
                            new SearchItemResponse(3L, "ITEM - 3", "IC000003", ItemStatus.ACTIVE),
                            new SearchItemResponse(4L, "ITEM - 4", "IC000004", ItemStatus.INACTIVE),
                            new SearchItemResponse(5L, "ITEM - 5", "IC000005", ItemStatus.ACTIVE)
                    );

            Page<SearchItemResponse> data = new PageImpl<>(contents, pageable, contents.size());

            SearchItemCondition condition = new SearchItemCondition("IC", null);

            given(itemRepository.search(condition, pageable)).willReturn(data);

            PageResponse<SearchItemResponse> response = itemService.getItemBySearch(condition, pageable);

            assertThat(response.pageInfo().page()).isEqualTo(1);
            assertThat(response.pageInfo().size()).isEqualTo(10);
            assertThat(response.pageInfo().totalElements()).isEqualTo(contents.size());
            assertThat(response.pageInfo().totalPages()).isEqualTo(1);

            assertThat(response.content().size()).isEqualTo(contents.size());
            assertThat(response.content().containsAll(contents));
        }
    }

    @Nested
    class change_base_price_test {
        @Test
        void change_base_price_success() {
            Item item = ItemFixture.create();

            given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));

            ChangeItemPriceRequest request = new ChangeItemPriceRequest(BigDecimal.valueOf(10000.00));
            itemService.changePrice(item.getId(), request);

            assertThat(item.getBasePrice()).isEqualTo(request.basePrice());
        }

        @Test
        void change_base_price_fail_with_not_found() {
            ChangeItemPriceRequest request = new ChangeItemPriceRequest(BigDecimal.valueOf(10000.00));

            given(itemRepository.findById(anyLong())).willReturn(Optional.empty());
            assertThatThrownBy(() -> itemService.changePrice(1L, request))
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }
}