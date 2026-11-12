package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.BudgetDao;
import com.workshop.refrigerator.dto.BudgetRequest;
import com.workshop.refrigerator.dto.BudgetResponse;
import com.workshop.refrigerator.exception.ConflictException;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.Budget;
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

class BudgetServiceTest {

    private final BudgetDao dao = mock(BudgetDao.class);
    private final BudgetService service = new BudgetService(dao);

    @Test
    void createReturnsSavedResponse() {
        when(dao.existsByYearAndMonth((short) 2026, (short) 11)).thenReturn(false);
        when(dao.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget budget = invocation.getArgument(0);
            budget.setId(1L);
            budget.setCreatedAt(OffsetDateTime.now());
            return budget;
        });

        BudgetResponse response = service.create(request("u1", "150.00", (short) 11, (short) 2026));

        assertEquals(1L, response.id());
        assertEquals("u1", response.userId());
        assertEquals(new BigDecimal("150.00"), response.monthlyLimit());
        assertEquals((short) 11, response.month());
        assertEquals((short) 2026, response.year());
        verify(dao).save(any(Budget.class));
    }

    @Test
    void createRejectsDuplicateYearAndMonth() {
        when(dao.existsByYearAndMonth((short) 2026, (short) 11)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.create(request("u1", "150.00", (short) 11, (short) 2026)));
    }

    @Test
    void findAllReturnsMappedList() {
        when(dao.findAll()).thenReturn(List.of(
                budget(1L, "u1", "100.00", (short) 10, (short) 2026),
                budget(2L, "u1", "200.00", (short) 11, (short) 2026)));

        List<BudgetResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals(new BigDecimal("200.00"), responses.get(1).monthlyLimit());
    }

    @Test
    void findByIdReturnsResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(budget(1L, "u1", "100.00", (short) 10, (short) 2026)));

        BudgetResponse response = service.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("u1", response.userId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateReturnsUpdatedResponse() {
        when(dao.findById(1L)).thenReturn(Optional.of(budget(1L, "u1", "100.00", (short) 10, (short) 2026)));
        when(dao.findByYearAndMonth((short) 2026, (short) 12)).thenReturn(Optional.empty());
        when(dao.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResponse response = service.update(1L, request("u1", "300.00", (short) 12, (short) 2026));

        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("300.00"), response.monthlyLimit());
        assertEquals((short) 12, response.month());
        verify(dao).save(any(Budget.class));
    }

    @Test
    void updateThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, request("u1", "300.00", (short) 12, (short) 2026)));
    }

    @Test
    void deleteRemovesEntity() {
        Budget budget = budget(1L, "u1", "100.00", (short) 10, (short) 2026);
        when(dao.findById(1L)).thenReturn(Optional.of(budget));

        service.delete(1L);

        verify(dao).delete(budget);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    private static BudgetRequest request(String userId, String monthlyLimit, short month, short year) {
        return new BudgetRequest(userId, new BigDecimal(monthlyLimit), month, year);
    }

    private static Budget budget(Long id, String userId, String monthlyLimit, short month, short year) {
        Budget budget = new Budget();
        budget.setId(id);
        budget.setUserId(userId);
        budget.setMonthlyLimit(new BigDecimal(monthlyLimit));
        budget.setMonth(month);
        budget.setYear(year);
        budget.setCreatedAt(OffsetDateTime.now());
        return budget;
    }
}
