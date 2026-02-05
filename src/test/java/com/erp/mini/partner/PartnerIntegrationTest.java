package com.erp.mini.partner;

import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.dto.AddPartnerRequest;
import com.erp.mini.partner.dto.UpdatePartnerRequest;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserTestDataFactory;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestLoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@ActiveProfiles("integration")
public class PartnerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserTestDataFactory factory;

    @Autowired
    private PartnerRepository partnerRepository;

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
    class add_partner_test {
        @Test
        void add_partner_success() throws Exception {
            partnerRepository.deleteAll();

            AddPartnerRequest request = new AddPartnerRequest(
                    "기성식품", PartnerType.CUSTOMER, null, null
            );

            mockMvc.perform(post("/api/partner")
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            Partner savedPartner = partnerRepository.findAll().get(0);

            assertThat(savedPartner.getName()).isEqualTo(request.name());
            assertThat(savedPartner.getType()).isEqualTo(request.type());
            assertThat(savedPartner.getCode()).isNotNull();
        }
    }

    @Nested
    class update_partner_test {
        @Test
        void update_partner_success() throws Exception {
            Partner supplier = partnerRepository.findById(1L)
                    .orElseThrow();

            assertThat(supplier.getPhone()).isNotNull();
            assertThat(supplier.getEmail()).isEqualTo("test@supplier.com");

            UpdatePartnerRequest request = new UpdatePartnerRequest("supplier@test.com", "");

            mockMvc.perform(patch("/api/partner/{partnerId}", supplier.getId())
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Partner findPartner = partnerRepository.findById(1L)
                    .orElseThrow();

            assertThat(findPartner.getPhone()).isNull();
            assertThat(findPartner.getEmail()).isEqualTo(request.email());
        }

        @Test
        void update_partner_fail_with_not_found() throws Exception {
            UpdatePartnerRequest request = new UpdatePartnerRequest("supplier@test.com", "");

            mockMvc.perform(patch("/api/partner/{partnerId}", 543254321432L)
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class search_partner_test {
        @Test
        void search_partner_success1() throws Exception {
            mockMvc.perform(get("/api/partner")
                            .param("keyword", "테스트 공급")
                            .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("테스트 공급사"))
                    .andExpect(jsonPath("$.data.pageInfo.size").value("10"))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value("1"))
                    .andDo(print());
        }

        @Test
        void search_partner_success2() throws Exception {
            mockMvc.perform(get("/api/partner")
                            .param("keyword", "테스트")
                            .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[*].name", hasItems("테스트 공급사", "테스트 고객사")))
                    .andExpect(jsonPath("$.data.pageInfo.size").value("10"))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value("2"))
                    .andDo(print());
        }
    }
}
