package edu.eci.arsw.blueprints.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllBlueprintsReturnsOkWithWrappedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getBlueprintByKnownAuthorAndNameReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints/john/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("house"));
    }

    @Test
    void getBlueprintByUnknownAuthorReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void createBlueprintReturnsCreated() throws Exception {
        String body = """
                { "author":"alice", "name":"office", "points":[{"x":1,"y":1},{"x":2,"y":2}] }
                """;
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    void createBlueprintWithBlankAuthorReturnsBadRequest() throws Exception {
        String body = """
                { "author":"", "name":"office", "points":[] }
                """;
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void creatingDuplicateBlueprintReturnsBadRequest() throws Exception {
        String body = """
                { "author":"john", "name":"house", "points":[] }
                """;
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addPointToExistingBlueprintReturnsAccepted() throws Exception {
        mockMvc.perform(put("/api/v1/blueprints/john/garage/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"x\":9, \"y\":9 }"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202));
    }

    @Test
    void addPointToUnknownBlueprintReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/blueprints/nobody/nothing/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"x\":1, \"y\":1 }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
