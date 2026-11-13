package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.FruitDao;
import com.workshop.refrigerator.dto.ProduceRequest;
import com.workshop.refrigerator.dto.ProduceResponse;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.Fruit;
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

class FruitServiceTest {

    private final FruitDao dao = mock(FruitDao.class);
    private final FruitService service = new FruitService(dao);

    @Test
    void createReturnsSavedResponse() {
        when(dao.save(any(Fruit.class))).thenAnswer(invocation -> {
            Fruit fruit = invocation.getArgument(0);
            fruit.setId(1L);
            fruit.setCreatedAt(OffsetDateTime.now());
            return fruit;
        });

        ProduceResponse response = service.create(request("u1", "Apple"));

        assertEquals(1L, response.id());
        assertEquals("u1", response.userId());
        assertEquals("Apple", response.name());
        assertEquals(new BigDecimal("5.00"), response.quantity());
        assertEquals("kg", response.unit());
        verify(dao).save(any(Fruit.class));
    }

    @Test
    void findAllReturnsMappedList() {
        when(dao.findAll()).thenReturn(List.of(
                fruit(1L, "u1", "Apple"),
                fruit(2L, "u1", "Pear")));

        List<ProduceResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals("Apple", responses.get(0).name());
        assertEquals("Pear", responses.get(1).name());
    }

    @Test
    void findByIdReturnsResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(fruit(1L, "u1", "Apple")));

        ProduceResponse response = service.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Apple", response.name());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateReturnsUpdatedResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(fruit(1L, "u1", "Apple")));
        when(dao.save(any(Fruit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProduceResponse response = service.update(1L, request("u1", "Green Apple"));

        assertEquals(1L, response.id());
        assertEquals("Green Apple", response.name());
        verify(dao).save(any(Fruit.class));
    }

    @Test
    void updateThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request("u1", "Green Apple")));
    }

    @Test
    void deleteRemovesEntity() {
        Fruit fruit = fruit(1L, "u1", "Apple");
        when(dao.findById(1L)).thenReturn(Optional.of(fruit));

        service.delete(1L);

        verify(dao).delete(fruit);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    private static ProduceRequest request(String userId, String name) {
        return new ProduceRequest(userId, name, new BigDecimal("5.00"), "kg", new BigDecimal("2.50"), null, null);
    }

    private static Fruit fruit(Long id, String userId, String name) {
        Fruit fruit = new Fruit();
        fruit.setId(id);
        fruit.setUserId(userId);
        fruit.setName(name);
        fruit.setQuantity(new BigDecimal("5.00"));
        fruit.setUnit("kg");
        fruit.setPrice(new BigDecimal("2.50"));
        fruit.setCreatedAt(OffsetDateTime.now());
        return fruit;
    }
}
