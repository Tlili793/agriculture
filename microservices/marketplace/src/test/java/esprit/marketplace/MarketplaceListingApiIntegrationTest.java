package esprit.marketplace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MarketplaceListingApiIntegrationTest {

    private static final UUID USER_ONE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_TWO = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsAndReadsListings() throws Exception {
        long id = createListing(USER_ONE, "Fresh tomatoes");

        mockMvc.perform(get("/api/marketplace/listings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userId").value(USER_ONE.toString()))
                .andExpect(jsonPath("$.title").value("Fresh tomatoes"))
                .andExpect(jsonPath("$.price").value(2.50))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/api/marketplace/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/marketplace/listings/user/{userId}", USER_ONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].userId").value(USER_ONE.toString()));
    }

    @Test
    void updatesOwnedListingAndKeepsOwner() throws Exception {
        long id = createListing(USER_ONE, "Fresh tomatoes");

        mockMvc.perform(put("/api/marketplace/listings/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingBody(USER_ONE, "Organic tomatoes", 12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ONE.toString()))
                .andExpect(jsonPath("$.title").value("Organic tomatoes"))
                .andExpect(jsonPath("$.quantity").value(12));
    }

    @Test
    void rejectsUpdateByAnotherUser() throws Exception {
        long id = createListing(USER_ONE, "Fresh tomatoes");

        mockMvc.perform(put("/api/marketplace/listings/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingBody(USER_TWO, "Stolen tomatoes", 12)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletesOwnedListing() throws Exception {
        long id = createListing(USER_ONE, "Fresh tomatoes");

        mockMvc.perform(delete("/api/marketplace/listings/{id}", id)
                        .param("userId", USER_ONE.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/marketplace/listings/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsDeleteByAnotherUser() throws Exception {
        long id = createListing(USER_ONE, "Fresh tomatoes");

        mockMvc.perform(delete("/api/marketplace/listings/{id}", id)
                        .param("userId", USER_TWO.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesCreateRequest() throws Exception {
        mockMvc.perform(post("/api/marketplace/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Missing owner","price":1.00,"quantity":1}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/marketplace/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingBody(USER_ONE, " ", 1)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/marketplace/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s","title":"Invalid price","price":-1.00,"quantity":1}
                                """.formatted(USER_ONE)))
                .andExpect(status().isBadRequest());
    }

    private long createListing(UUID userId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/marketplace/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingBody(userId, title, 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private String listingBody(UUID userId, String title, int quantity) {
        return """
                {
                  "userId": "%s",
                  "title": "%s",
                  "description": "Farm fresh",
                  "price": 2.50,
                  "quantity": %d
                }
                """.formatted(userId, title, quantity);
    }
}
