package com.repifuzz;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RepifuzzAssignmnetApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void incidentEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/ims/incidents/RMG000002026"))
                .andExpect(status().isUnauthorized());
    }

}
