
package com.surest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.dto.MemberDTO;
import com.surest.exception.GlobalExceptionHandler;
import com.surest.exception.MemberAlreadyExistsException;
import com.surest.exception.MemberNotFoundException;
import com.surest.security.SecurityConfig;
import com.surest.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    private MemberDTO sampleMember(UUID id) {
        MemberDTO dto = new MemberDTO(UUID.randomUUID(), "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", 1);
        dto.setId(id);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setVersion(1);
        return dto;
    }

    private String sampleMemberJson() {
        return """
            {
              "firstName": "John",
              "lastName": "Doe",
              "email": "john.doe@example.com",
              "dateOfBirth": "1990-01-01"
            }
            """;
    }

    @Test
    @DisplayName("GET /api/v1/members as USER returns paged content and calls service with correct params")
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllMembers_user_ok() throws Exception {
        UUID id = UUID.randomUUID();
        Page<MemberDTO> page = new PageImpl<>(
                List.of(sampleMember(id)),
                PageRequest.of(0, 10),
                1
        );

        when(memberService.getAllMembers(0, 10, "lastName,asc", "John", "Doe")).thenReturn(page);

        mockMvc.perform(get("/api/v1/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        // Verify args
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> sortCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> firstNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> lastNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(memberService, times(1)).getAllMembers(
                pageCaptor.capture(),
                sizeCaptor.capture(),
                sortCaptor.capture(),
                firstNameCaptor.capture(),
                lastNameCaptor.capture()
        );

        assert pageCaptor.getValue() == 0;
        assert sizeCaptor.getValue() == 10;
        assert "lastName,asc".equals(sortCaptor.getValue());
        assert "John".equals(firstNameCaptor.getValue());
        assert "Doe".equals(lastNameCaptor.getValue());
    }

    @Test
    @DisplayName("GET /api/v1/members/{id} as USER returns a member")
    @WithMockUser(username = "user", roles = {"USER"})
    void getMemberById_user_ok() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO dto = sampleMember(id);
        when(memberService.getMemberById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/members/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(memberService, times(1)).getMemberById(id);
    }

    @Test
    @DisplayName("POST /api/v1/members as ADMIN creates a member and returns 201")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createMember_admin_created() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO saved = sampleMember(id);
        when(memberService.createMember(any(MemberDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleMemberJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(memberService, times(1)).createMember(any(MemberDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/members/{id} as ADMIN updates and returns 200")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMember_admin_ok() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO updated = sampleMember(id);
        updated.setFirstName("Johnny");

        when(memberService.updateMember(eq(id), any(MemberDTO.class))).thenReturn(updated);

        String updateJson = """
            {
              "firstName": "Johnny",
              "lastName": "Doe",
              "email": "john.doe@example.com",
              "dateOfBirth": "1990-01-01",
              "version": 1
            }
            """;

        mockMvc.perform(put("/api/v1/members/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Johnny"));

        verify(memberService, times(1)).updateMember(eq(id), any(MemberDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/members/{id} as ADMIN returns 204")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteMember_admin_noContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(memberService).deleteMember(id);

        mockMvc.perform(delete("/api/v1/members/{id}", id))
                .andExpect(status().isNoContent());

        verify(memberService, times(1)).deleteMember(id);
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint is 401 (JWT required)")
    void unauthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("POST /members as USER is forbidden (403) by @PreAuthorize")
    @WithMockUser(username = "user", roles = {"USER"})
    void createMember_user_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleMemberJson()))
                .andExpect(status().isForbidden());

        verify(memberService, never()).createMember(any(MemberDTO.class));
    }

    @Test
    @DisplayName("POST /members validation errors return 400 with ErrorResponse from advice")
    @WithMockUser(roles = "ADMIN")
    void createMember_validationErrors_badRequest() throws Exception {
        String invalidJson = """
            {
              "firstName": "",
              "lastName": "",
              "email": "not-an-email",
              "dateOfBirth": null,
              "version": 0
            }
            """;

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/members"));
    }

    @Test
    @DisplayName("GET /members/{id} maps MemberNotFoundException to 404 with advice body")
    @WithMockUser(roles = "USER")
    void getMemberById_notFound_mapsToAdvice() throws Exception {
        UUID id = UUID.randomUUID();
        when(memberService.getMemberById(id)).thenThrow(new MemberNotFoundException(id));

        mockMvc.perform(get("/api/v1/members/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/members/" + id));
    }

    @Test
    @DisplayName("POST /members maps MemberAlreadyExistsException to 409 with advice body")
    @WithMockUser(roles = "ADMIN")
    void createMember_conflict_mapsToAdvice() throws Exception {
        when(memberService.createMember(any(MemberDTO.class)))
                .thenThrow(new MemberAlreadyExistsException("john.doe@example.com"));

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleMemberJson()))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/members"));
    }
}
