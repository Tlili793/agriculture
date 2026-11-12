package com.awd.logistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LogisticsApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String WAREHOUSE = """
            {"name":"Tunis Central Warehouse","fridgeCapacity":500.0,"normalCapacity":2000.0,
             "addressLocale":"Zone Industrielle, Ben Arous"}
            """;

    private static final String DELIVERY = """
            {"orderId":42,"destination":"12 Avenue Habib Bourguiba, Tunis"}
            """;

    private long create(String url, String body) throws Exception {
        String json = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        return node.get("id").asLong();
    }

    @Test
    void hiEndpointGreets() throws Exception {
        mvc.perform(get("/api/logistics/hi"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hi, from Ines"));
    }

    @Test
    void swaggerDocsAreServed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Logistics Microservice API"))
                .andExpect(jsonPath("$.paths['/api/logistics/hi']").exists())
                .andExpect(jsonPath("$.paths['/api/deliveries']").exists())
                .andExpect(jsonPath("$.paths['/api/warehouses']").exists());
    }

    @Test
    void warehouseCrud() throws Exception {
        long id = create("/api/warehouses", WAREHOUSE);

        mvc.perform(get("/api/warehouses/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tunis Central Warehouse"))
                .andExpect(jsonPath("$.fridgeCapacity").value(500.0));

        mvc.perform(put("/api/warehouses/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Tunis Main","fridgeCapacity":600.0,"normalCapacity":2500.0,
                                 "addressLocale":"Ben Arous"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tunis Main"))
                .andExpect(jsonPath("$.normalCapacity").value(2500.0));

        mvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(delete("/api/warehouses/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/warehouses/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void warehouseValidation() throws Exception {
        mvc.perform(post("/api/warehouses").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","fridgeCapacity":-1,"normalCapacity":10,"addressLocale":"x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.fridgeCapacity").exists());
    }

    @Test
    void deliveryDefaultsToPending() throws Exception {
        long id = create("/api/deliveries", DELIVERY);

        mvc.perform(get("/api/deliveries/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(42))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.dispatchedAt").doesNotExist());
    }

    @Test
    void deliveryStatusChangeSetsDispatchedAt() throws Exception {
        long id = create("/api/deliveries", DELIVERY);

        mvc.perform(patch("/api/deliveries/" + id + "/status").param("status", "DISPATCHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.dispatchedAt").exists());

        mvc.perform(patch("/api/deliveries/" + id + "/status").param("status", "NOPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deliveryUpdateAndFilters() throws Exception {
        long id = create("/api/deliveries", DELIVERY);
        create("/api/deliveries", """
                {"orderId":43,"status":"DELIVERED","destination":"Sfax"}
                """);

        mvc.perform(put("/api/deliveries/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":42,"status":"IN_TRANSIT","destination":"Sousse"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.destination").value("Sousse"));

        mvc.perform(get("/api/deliveries"))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/deliveries").param("status", "DELIVERED"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(43));
        mvc.perform(get("/api/deliveries").param("orderId", "42"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_TRANSIT"));
    }

    @Test
    void deliveryValidationAndErrors() throws Exception {
        mvc.perform(post("/api/deliveries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destination\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.orderId").exists())
                .andExpect(jsonPath("$.errors.destination").exists());

        mvc.perform(post("/api/deliveries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":1,\"status\":\"UNKNOWN\",\"destination\":\"Tunis\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/deliveries/999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/deliveries/999")).andExpect(status().isNotFound());
    }

    @Test
    void deliveryDelete() throws Exception {
        long id = create("/api/deliveries", DELIVERY);
        mvc.perform(delete("/api/deliveries/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/deliveries/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void assignmentRequestAndAcceptance() throws Exception {
        long id = create("/api/deliveries", DELIVERY);
        UUID transporterId = UUID.randomUUID();

        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"" + transporterId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedTransporterId").value(transporterId.toString()))
                .andExpect(jsonPath("$.assignedTransporterId").doesNotExist());

        mvc.perform(get("/api/deliveries/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedTransporterId").value(transporterId.toString()));

        mvc.perform(post("/api/deliveries/" + id + "/assignment-accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTransporterId").value(transporterId.toString()))
                .andExpect(jsonPath("$.requestedTransporterId").doesNotExist())
                .andExpect(jsonPath("$.assignedAt").exists());

        mvc.perform(post("/api/deliveries/" + id + "/assignment-accept"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void assignmentRequestConflictsAndValidation() throws Exception {
        long id = create("/api/deliveries", DELIVERY);
        UUID firstTransporter = UUID.randomUUID();

        mvc.perform(post("/api/deliveries/" + id + "/assignment-accept"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"" + firstTransporter + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"" + firstTransporter + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/deliveries/" + id + "/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"not-a-uuid\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/deliveries/999/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transporterId\":\"" + firstTransporter + "\"}"))
                .andExpect(status().isNotFound());
    }
}