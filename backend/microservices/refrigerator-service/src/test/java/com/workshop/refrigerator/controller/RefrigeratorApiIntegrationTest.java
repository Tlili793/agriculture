package com.workshop.refrigerator.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RefrigeratorApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fruitCrudLifecycle() throws Exception {
        String body = """
                {"userId":"u1","name":"Apple","quantity":5,"unit":"kg","price":2.50,
                 "purchaseDate":"2026-11-01","expirationDate":"2026-11-20"}
                """;

        String created = mockMvc.perform(post("/refrigerator/fruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.name").value("Apple"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andReturn().getResponse().getContentAsString();

        int id = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/refrigerator/fruits/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));

        mockMvc.perform(get("/refrigerator/fruits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(put("/refrigerator/fruits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("Apple", "Green Apple")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Green Apple"));

        mockMvc.perform(delete("/refrigerator/fruits/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/refrigerator/fruits/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingUserIdDefaultsToTest() throws Exception {
        String body = """
                {"name":"Banana","quantity":2,"unit":"kg","price":1.00}
                """;

        mockMvc.perform(post("/refrigerator/fruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("test"));
    }

    @Test
    void createMealReturnsCreated() throws Exception {
        String body = """
                {"name":"Salad","mealType":"LUNCH","estimatedCost":4.25,"plannedDate":"2026-11-12"}
                """;

        mockMvc.perform(post("/refrigerator/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.userId").value("test"));
    }

    @Test
    void validationFailureReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/refrigerator/fruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void expirationBeforePurchaseReturnsBadRequest() throws Exception {
        String body = """
                {"name":"Pear","purchaseDate":"2026-11-20","expirationDate":"2026-11-01"}
                """;

        mockMvc.perform(post("/refrigerator/fruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateBudgetReturnsConflict() throws Exception {
        String body = "{\"userId\":\"u1\",\"monthlyLimit\":100.00,\"month\":11,\"year\":2026}";

        mockMvc.perform(post("/refrigerator/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/refrigerator/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
