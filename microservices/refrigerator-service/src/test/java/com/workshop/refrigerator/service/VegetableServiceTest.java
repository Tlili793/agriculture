package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.VegetableDao;
import com.workshop.refrigerator.dto.ProduceRequest;
import com.workshop.refrigerator.dto.ProduceResponse;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.Vegetable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VegetableServiceTest {

    private final VegetableDao dao = mock(VegetableDao.class);
    private final VegetableService service = new VegetableService(dao);

    @Test
    void createReturnsSavedResponse() {
        when(dao.save(any(Vegetable.class))).thenAnswer(invocation -> {
            Vegetable vegetable = invocation.getArgument(0);
            vegetable.setId(1L);
            vegetable.setCreatedAt(OffsetDateTime.now());
            return vegetable;
        });

        ProduceResponse response = service.create(request("u1", "Carrot"));

        assertEquals(1L, response.id());
        assertEquals("u1", response.userId());
        assertEquals("Carrot", response.name());
        verify(dao).save(any(Vegetable.class));
    }

    @Test
    void findAllReturnsMappedList() {
        when(dao.findAll()).thenReturn(List.of(
                vegetable(1L, "u1", "Carrot"),
                vegetable(2L, "u1", "Potato")));

        List<ProduceResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals("Carrot", responses.get(0).name());
        assertEquals("Potato", responses.get(1).name());
    }

    @Test
    void findByIdReturnsResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(vegetable(1L, "u1", "Carrot")));

        ProduceResponse response = service.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Carrot", response.name());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateReturnsUpdatedResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(vegetable(1L, "u1", "Carrot")));
        when(dao.save(any(Vegetable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProduceResponse response = service.update(1L, request("u1", "Baby Carrot"));

        assertEquals(1L, response.id());
        assertEquals("Baby Carrot", response.name());
        verify(dao).save(any(Vegetable.class));
    }

    @Test
    void updateThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request("u1", "Baby Carrot")));
    }

    @Test
    void deleteRemovesEntity() {
        Vegetable vegetable = vegetable(1L, "u1", "Carrot");
        when(dao.findById(1L)).thenReturn(Optional.of(vegetable));

        service.delete(1L);

        verify(dao).delete(vegetable);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    private static ProduceRequest request(String userId, String name) {
        return new ProduceRequest(userId, name, new BigDecimal("3.00"), "kg", new BigDecimal("1.20"), null, null);
    }

    private static Vegetable vegetable(Long id, String userId, String name) {
        Vegetable vegetable = new Vegetable();
        vegetable.setId(id);
        vegetable.setUserId(userId);
        vegetable.setName(name);
        vegetable.setQuantity(new BigDecimal("3.00"));
        vegetable.setUnit("kg");
        vegetable.setPrice(new BigDecimal("1.20"));
        vegetable.setCreatedAt(OffsetDateTime.now());
        return vegetable;
    }
}
