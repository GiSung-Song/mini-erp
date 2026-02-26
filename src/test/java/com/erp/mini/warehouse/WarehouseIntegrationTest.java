package com.erp.mini.warehouse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserTestDataFactory;
import com.erp.mini.util.IntegrationTest;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestLoginUser;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.dto.AddWarehouseRequest;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@IntegrationTest
public class WarehouseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserTestDataFactory factory;

    @Autowired
    private WarehouseRepository warehouseRepository;

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

    private Warehouse createWarehouse(String name, String location) {
        Warehouse w = Warehouse.createWarehouse(name, location, WarehouseStatus.ACTIVE);
        return warehouseRepository.save(w);
    }

    @Nested
    class add_warehouse_test {
        @Test
        void add_warehouse_success() throws Exception {
            warehouseRepository.deleteAll();

            AddWarehouseRequest request = new AddWarehouseRequest(
                    "울산 창고", "울산광역시 어딘가", WarehouseStatus.ACTIVE);

            mockMvc.perform(post("/api/warehouse")
                    .with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            Warehouse warehouse = warehouseRepository.findAll().get(0);

            assertThat(warehouse.getName()).isEqualTo(request.name());
            assertThat(warehouse.getLocation()).isEqualTo(request.location());
            assertThat(warehouse.getStatus()).isEqualTo(request.status());
            assertThat(warehouse.getCode()).isNotNull();
        }
    }

    @Nested
    class deactivate_warehouse_test {
        @Test
        void deactivate_warehouse_success() throws Exception {
            Warehouse warehouse = Warehouse.createWarehouse("테스트 창고", "location", WarehouseStatus.ACTIVE);
            warehouseRepository.save(warehouse);
            em.flush();

            assertThat(warehouse.getStatus()).isEqualTo(WarehouseStatus.ACTIVE);

            mockMvc.perform(patch("/api/warehouse/{warehouseId}/deactivate", warehouse.getId())
                    .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Warehouse deactiveWarehouse = warehouseRepository.findById(warehouse.getId())
                    .orElseThrow();

            assertThat(deactiveWarehouse.getStatus()).isEqualTo(WarehouseStatus.INACTIVE);
        }

        @Test
        void deactivate_warehouse_fail_with_not_found() throws Exception {
            mockMvc.perform(patch("/api/warehouse/{warehouseId}/deactivate", 432145321L)
                    .with(authentication(auth)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class activate_warehouse_test {
        @Test
        void activate_warehouse_success() throws Exception {
            Warehouse warehouse = Warehouse.createWarehouse("테스트 창고2", "location2", WarehouseStatus.INACTIVE);
            warehouseRepository.save(warehouse);
            em.flush();
            assertThat(warehouse.getStatus()).isEqualTo(WarehouseStatus.INACTIVE);

            mockMvc.perform(patch("/api/warehouse/{warehouseId}/activate", warehouse.getId())
                    .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Warehouse activatedWarehouse = warehouseRepository.findById(warehouse.getId()).orElseThrow();
            assertThat(activatedWarehouse.getStatus()).isEqualTo(WarehouseStatus.ACTIVE);
        }

        @Test
        void activate_warehouse_fail_with_not_found() throws Exception {
            mockMvc.perform(patch("/api/warehouse/{warehouseId}/activate", 432145321L)
                    .with(authentication(auth)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class search_warehouse_test {
        @Test
        void search_warehouse_success_by_name() throws Exception {
            warehouseRepository.deleteAll();
            createWarehouse("테스트 1창고", "어딘가");

            mockMvc.perform(get("/api/warehouse")
                    .with(authentication(auth))
                    .param("keyword", "테스트 1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("테스트 1창고"))
                    .andExpect(jsonPath("$.data.pageInfo.size").value("10"))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value("1"))
                    .andDo(print());
        }

        @Test
        void search_warehouse_success_by_location() throws Exception {
            warehouseRepository.deleteAll();
            createWarehouse("테스트 1창고", "테스트시 테스트구 테스트동 테스트지역 12-34");
            createWarehouse("테스트 2창고", "테스트시 테스트구 테스트동 테스트지역 56-78");

            mockMvc.perform(get("/api/warehouse")
                    .with(authentication(auth))
                    .param("keyword", "테스트시"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[*].location",
                            hasItems("테스트시 테스트구 테스트동 테스트지역 12-34",
                                    "테스트시 테스트구 테스트동 테스트지역 56-78")))
                    .andExpect(jsonPath("$.data.content[*].name",
                            hasItems("테스트 1창고", "테스트 2창고")))
                    .andExpect(jsonPath("$.data.pageInfo.size").value("10"))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value("2"))
                    .andDo(print());
        }

        @Test
        void search_warehouse_success_by_none() throws Exception {
            mockMvc.perform(get("/api/warehouse")
                    .with(authentication(auth))
                    .param("keyword", "테트리스"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageInfo.size").value("10"))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value("0"))
                    .andDo(print());
        }
    }
}
