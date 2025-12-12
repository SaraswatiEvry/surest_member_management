
package com.surest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.dto.MemberDTO;
import com.surest.exception.GlobalExceptionHandler;
import com.surest.security.JwtRequestFilter;
import com.surest.security.SecurityConfig;
import com.surest.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MemberController with real Spring Security:
 * - Roles enforced by SecurityConfig (ADMIN/USER vs unauthenticated).
 * - Method-level validation (@Valid MemberDTO).
 * - JwtRequestFilter is overridden to NO-OP for test requests.
 * Ensure method security is enabled in app (you already have @EnableMethodSecurity).
 */

@WebMvcTest(MemberController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MemberControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    // Override JwtRequestFilter to NO-OP so we can use @WithMockUser without real JWT.
    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    private static final String BASE = "/api/v1/members";

    @BeforeEach
    void makeJwtFilterNoOp() throws Exception {
        // Let the request pass through without enforcing JWT
        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtRequestFilter).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class),
                any(FilterChain.class)
        );
    }

    // -------------------------------------------
    // GET /api/v1/members (ADMIN/USER allowed)
    // -------------------------------------------

    @Test
    @DisplayName("GET /members as USER → 200 OK with paged content")
    @WithMockUser(username = "user1", roles = {"USER"})
    void getAllMembers_asUser_ok() throws Exception {
        MemberDTO m1 = new MemberDTO(UUID.randomUUID(), "Alice", "Johnson",
                LocalDate.of(1990, 1, 15), "alice.johnson@example.com", 0);
        MemberDTO m2 = new MemberDTO(UUID.randomUUID(), "Bob", "Smith",
                LocalDate.of(1985, 5, 20), "bob.smith@example.com", 1);

        Page<MemberDTO> page = new PageImpl<>(List.of(m1, m2), PageRequest.of(0, 10), 2);
        when(memberService.getAllMembers(eq(0), eq(10), eq("lastName,asc"), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get(BASE)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].firstName", is("Alice")))
                .andExpect(jsonPath("$.content[0].lastName", is("Johnson")))
                .andExpect(jsonPath("$.content[0].email", is("alice.johnson@example.com")))
                .andExpect(jsonPath("$.content[1].firstName", is("Bob")))
                .andExpect(jsonPath("$.content[1].lastName", is("Smith")))
                .andExpect(jsonPath("$.totalElements", is(2)));

        verify(memberService).getAllMembers(0, 10, "lastName,asc", null, null);
    }

    @Test
    @DisplayName("GET /members unauthenticated → 401 Unauthorized")
    void getAllMembers_unauthenticated_401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
        verify(memberService, never()).getAllMembers(anyInt(), anyInt(), anyString(), any(), any());
    }

    // -------------------------------------------
    // GET /api/v1/members/{id} (ADMIN/USER allowed)
    // -------------------------------------------

    @Test
    @DisplayName("GET /members/{id} as USER → 200 OK")
    @WithMockUser(username = "user1", roles = {"USER"})
    void getMemberById_asUser_ok() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO dto = new MemberDTO(id, "Charlie", "Brown",
                LocalDate.of(1975, 3, 10), "charlie.brown@example.com", 2);

        when(memberService.getMemberById(eq(id))).thenReturn(dto);

        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.firstName", is("Charlie")))
                .andExpect(jsonPath("$.lastName", is("Brown")))
                .andExpect(jsonPath("$.email", is("charlie.brown@example.com")))
                .andExpect(jsonPath("$.version", is(2)));

        verify(memberService).getMemberById(id);
    }

    @Test
    @DisplayName("GET /members/{id} unauthenticated → 401 Unauthorized")
    void getMemberById_unauthenticated_401() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isUnauthorized());
        verify(memberService, never()).getMemberById(any(UUID.class));
    }

    // -------------------------------------------
    // POST /api/v1/members (ADMIN only)
    // -------------------------------------------

    @Test
    @DisplayName("POST /members as ADMIN → 201 Created with body")
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void createMember_asAdmin_created() throws Exception {
        MemberDTO requestDto = new MemberDTO(
                null, "John", "Doe",
                LocalDate.of(1999, 12, 31), "john.doe@example.com", 0
        );
        MemberDTO savedDto = new MemberDTO(
                UUID.randomUUID(), requestDto.getFirstName(), requestDto.getLastName(),
                requestDto.getDateOfBirth(), requestDto.getEmail(), requestDto.getVersion()
        );

        when(memberService.createMember(any(MemberDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(savedDto.getId().toString())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.version", is(0)));

        ArgumentCaptor<MemberDTO> captor = ArgumentCaptor.forClass(MemberDTO.class);
        verify(memberService).createMember(captor.capture());
        MemberDTO passed = captor.getValue();
        // basic payload sanity
        assert "John".equals(passed.getFirstName());
        assert "Doe".equals(passed.getLastName());
        assert "john.doe@example.com".equals(passed.getEmail());
        assert LocalDate.of(1999, 12, 31).equals(passed.getDateOfBirth());
    }

    @Test
    @DisplayName("POST /members as USER → 403 Forbidden")
    @WithMockUser(username = "user1", roles = {"USER"})
    void createMember_asUser_forbidden() throws Exception {
        MemberDTO requestDto = new MemberDTO(
                null, "Jane", "Roe",
                LocalDate.of(2000, 1, 1), "jane.roe@example.com", 0
        );

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(memberService, never()).createMember(any());
    }

    @Test
    @DisplayName("POST /members unauthenticated → 401 Unauthorized")
    void createMember_unauthenticated_401() throws Exception {
        MemberDTO requestDto = new MemberDTO(
                null, "Unauth", "User",
                LocalDate.of(1995, 6, 1), "unauth.user@example.com", 0
        );

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());

        verify(memberService, never()).createMember(any());
    }

    @Test
    @DisplayName("POST /members as ADMIN with invalid payload → 400 Bad Request")
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void createMember_asAdmin_invalid_400() throws Exception {
        // Invalid by MemberDTO: blank names, null DOB, invalid email, negative version
        String invalidJson = """
            {
              "firstName": "",
              "lastName": "",
              "dateOfBirth": null,
              "email": "not-an-email",
              "version": -1
            }
            """;

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).createMember(any());
    }

    // -------------------------------------------
    // PUT /api/v1/members/{id} (ADMIN only)
    // -------------------------------------------

    @Test
    @DisplayName("PUT /members/{id} as ADMIN → 200 OK")
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void updateMember_asAdmin_ok() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO requestDto = new MemberDTO(
                id, "Updated", "Member",
                LocalDate.of(1988, 7, 21), "updated.member@example.com", 3
        );

        when(memberService.updateMember(eq(id), any(MemberDTO.class))).thenReturn(requestDto);

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Member")))
                .andExpect(jsonPath("$.email", is("updated.member@example.com")))
                .andExpect(jsonPath("$.version", is(3)));

        verify(memberService).updateMember(eq(id), any(MemberDTO.class));
    }

    @Test
    @DisplayName("PUT /members/{id} as USER → 403 Forbidden")
    @WithMockUser(username = "user1", roles = {"USER"})
    void updateMember_asUser_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO requestDto = new MemberDTO(
                id, "Updated", "Member",
                LocalDate.of(1988, 7, 21), "updated.member@example.com", 3
        );

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(memberService, never()).updateMember(any(), any());
    }

    @Test
    @DisplayName("PUT /members/{id} unauthenticated → 401 Unauthorized")
    void updateMember_unauthenticated_401() throws Exception {
        UUID id = UUID.randomUUID();
        MemberDTO requestDto = new MemberDTO(
                id, "Updated", "Member",
                LocalDate.of(1988, 7, 21), "updated.member@example.com", 3
        );

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());

        verify(memberService, never()).updateMember(any(), any());
    }

    @Test
    @DisplayName("PUT /members/{id} as ADMIN with invalid payload → 400 Bad Request")
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void updateMember_asAdmin_invalid_400() throws Exception {
        UUID id = UUID.randomUUID();

        String invalidJson = """
            {
              "id": "%s",
              "firstName": "",
              "lastName": "",
              "dateOfBirth": null,
              "email": "invalid",
              "version": -10
            }
            """.formatted(id);

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).updateMember(any(), any());
    }

    // -------------------------------------------
    // DELETE /api/v1/members/{id} (ADMIN only)
    // -------------------------------------------

    @Test
    @DisplayName("DELETE /members/{id} as ADMIN → 204 No Content")
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void deleteMember_asAdmin_noContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(memberService).deleteMember(eq(id));

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(memberService).deleteMember(id);
    }

    @Test
    @DisplayName("DELETE /members/{id} as USER → 403 Forbidden")
    @WithMockUser(username = "user1", roles = {"USER"})
    void deleteMember_asUser_forbidden() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isForbidden());

        verify(memberService, never()).deleteMember(any());
    }

    @Test
    @DisplayName("DELETE /members/{id} unauthenticated → 401 Unauthorized")
    void deleteMember_unauthenticated_401() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isUnauthorized());

        verify(memberService, never()).deleteMember(any());
    }
}
