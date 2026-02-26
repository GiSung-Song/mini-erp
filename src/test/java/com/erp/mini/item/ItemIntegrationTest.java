package com.erp.mini.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.dto.AddItemRequest;
import com.erp.mini.item.dto.ChangeItemPriceRequest;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserTestDataFactory;
import com.erp.mini.util.IntegrationTest;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestLoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@IntegrationTest
public class ItemIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserTestDataFactory factory;

        @Autowired
        private ItemRepository itemRepository;

        @PersistenceContext
        private EntityManager em;

        private Authentication auth;

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
                TestContainerManager.registerMySQL(registry);
        }

        @BeforeEach
        void setUp() {
                User user = factory.createUser("tester", "EMP-0001");

                auth = TestLoginUser.setAuthLogin(user);
        }

        @Nested
        class add_item_test {
                @Test
                void add_item_success() throws Exception {
                        AddItemRequest request = new AddItemRequest(
                                        "테스트 상품", BigDecimal.valueOf(10000), ItemStatus.ACTIVE);

                        mockMvc.perform(post("/api/item")
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(request)))
                                        .andExpect(status().isCreated())
                                        .andDo(print());

                        assertThat(itemRepository.findAll().size()).isEqualTo(1);

                        Item findItem = itemRepository.findByCode("IC000001")
                                        .orElseThrow();

                        assertThat(findItem.getBasePrice()).isEqualTo(request.basePrice());
                        assertThat(findItem.getName()).isEqualTo(request.name());
                        assertThat(findItem.getStatus()).isEqualTo(request.itemStatus());
                }
        }

        @Nested
        class deactivate_item_test {
                @Test
                void deactivate_item_success() throws Exception {
                        Item testItem = Item.createItem("테스트 아이템", "TEST_ITEM_001", BigDecimal.valueOf(1000),
                                        ItemStatus.ACTIVE);
                        itemRepository.save(testItem);
                        em.flush();

                        assertThat(testItem.getStatus()).isEqualTo(ItemStatus.ACTIVE);

                        mockMvc.perform(delete("/api/item/{itemId}", testItem.getId())
                                        .with(authentication(auth)))
                                        .andExpect(status().isOk())
                                        .andDo(print());

                        Item findItem = itemRepository.findById(testItem.getId())
                                        .orElseThrow();

                        assertThat(findItem.getStatus()).isEqualTo(ItemStatus.INACTIVE);
                }

                @Test
                void deactivate_item_fail_with_not_found() throws Exception {
                        mockMvc.perform(delete("/api/item/{itemId}", 43214321423L)
                                        .with(authentication(auth)))
                                        .andExpect(status().isNotFound())
                                        .andDo(print());
                }
        }

        @Nested
        class search_item_test {
                @Test
                void search_item_success() throws Exception {
                        Item item1 = Item.createItem("상품1", "ITEM_SEARCH_1", BigDecimal.valueOf(1000),
                                        ItemStatus.ACTIVE);
                        Item item2 = Item.createItem("상품2", "ITEM_SEARCH_2", BigDecimal.valueOf(2000),
                                        ItemStatus.ACTIVE);
                        itemRepository.save(item1);
                        itemRepository.save(item2);
                        em.flush();

                        mockMvc.perform(get("/api/item")
                                        .with(authentication(auth))
                                        .param("code", "ITEM"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                                        .andExpect(jsonPath("$.data.pageInfo.page").value(1))
                                        .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2))
                                        .andDo(print());
                }
        }

        @Nested
        class change_price_test {
                @Test
                void change_price_success() throws Exception {
                        Item testItem = Item.createItem("가격 변경 테스트", "TEST_PRICE_001", BigDecimal.valueOf(1000),
                                        ItemStatus.ACTIVE);
                        itemRepository.save(testItem);
                        em.flush();

                        assertThat(testItem.getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));

                        ChangeItemPriceRequest request = new ChangeItemPriceRequest(
                                        BigDecimal.valueOf(15000));

                        mockMvc.perform(patch("/api/item/{itemId}", testItem.getId())
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(request)))
                                        .andExpect(status().isOk())
                                        .andDo(print());

                        Item findItem = itemRepository.findById(testItem.getId())
                                        .orElseThrow();

                        assertThat(findItem.getBasePrice()).isEqualByComparingTo(request.basePrice());
                }

                @Test
                void change_price_fail_with_not_found() throws Exception {
                        ChangeItemPriceRequest request = new ChangeItemPriceRequest(
                                        BigDecimal.valueOf(15000));

                        mockMvc.perform(patch("/api/item/{itemId}", 643254312L)
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(request)))
                                        .andExpect(status().isNotFound())
                                        .andDo(print());
                }
        }
}
