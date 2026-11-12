package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.MealDao;
import com.workshop.refrigerator.dto.MealRequest;
import com.workshop.refrigerator.dto.MealResponse;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.Meal;
import com.workshop.refrigerator.model.MealType;
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

class MealServiceTest {

    private final MealDao dao = mock(MealDao.class);
    private final MealService service = new MealService(dao);

    @Test
    void createReturnsSavedResponse() {
        when(dao.save(any(Meal.class))).thenAnswer(invocation -> {
            Meal meal = invocation.getArgument(0);
            meal.setId(1L);
            meal.setCreatedAt(OffsetDateTime.now());
            return meal;
        });

        MealResponse response = service.create(request("u1", "Salad"));

        assertEquals(1L, response.id());
        assertEquals("u1", response.userId());
        assertEquals("Salad", response.name());
        assertEquals(MealType.LUNCH, response.mealType());
        verify(dao).save(any(Meal.class));
    }

    @Test
    void findAllReturnsMappedList() {
        when(dao.findAll()).thenReturn(List.of(
                meal(1L, "u1", "Salad"),
                meal(2L, "u1", "Soup")));

        List<MealResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals("Salad", responses.get(0).name());
        assertEquals("Soup", responses.get(1).name());
    }

    @Test
    void findByIdReturnsResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(meal(1L, "u1", "Salad")));

        MealResponse response = service.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Salad", response.name());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateReturnsUpdatedResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(meal(1L, "u1", "Salad")));
        when(dao.save(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MealResponse response = service.update(1L, request("u1", "Caesar Salad"));

        assertEquals(1L, response.id());
        assertEquals("Caesar Salad", response.name());
        verify(dao).save(any(Meal.class));
    }

    @Test
    void updateThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request("u1", "Caesar Salad")));
    }

    @Test
    void deleteRemovesEntity() {
        Meal meal = meal(1L, "u1", "Salad");
        when(dao.findById(1L)).thenReturn(Optional.of(meal));

        service.delete(1L);

        verify(dao).delete(meal);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    private static MealRequest request(String userId, String name) {
        return new MealRequest(userId, name, "Fresh greens", MealType.LUNCH, null, new BigDecimal("4.25"), null);
    }

    private static Meal meal(Long id, String userId, String name) {
        Meal meal = new Meal();
        meal.setId(id);
        meal.setUserId(userId);
        meal.setName(name);
        meal.setDescription("Fresh greens");
        meal.setMealType(MealType.LUNCH);
        meal.setEstimatedCost(new BigDecimal("4.25"));
        meal.setCreatedAt(OffsetDateTime.now());
        return meal;
    }
}
