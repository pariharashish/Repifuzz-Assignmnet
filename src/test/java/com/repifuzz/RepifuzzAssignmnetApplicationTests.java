package com.repifuzz;

import com.repifuzz.EntityDTO.RegisterUserRequest;
import com.repifuzz.EntityDTO.UserResponse;
import com.repifuzz.service.UserService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@AutoConfigureMockMvc
class RepifuzzAssignmnetApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void incidentEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/ims/incidents/RMG000002026"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void registrationReturnsSafeUserResponseWithoutPassword() throws Exception {
        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(UserResponse.builder()
                .id(1L)
                .username("reporter")
                .email("reporter@example.com")
                .build());

        mockMvc.perform(post("/api/ims/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"reporter","email":"reporter@example.com","password":"not-returned"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("reporter@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registrationRejectsInvalidInputWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/ims/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(userService);
    }

}
