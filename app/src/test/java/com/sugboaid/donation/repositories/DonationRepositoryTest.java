package com.sugboaid.donation.repositories;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.utils.SharedPreferencesHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for DonationRepository
 * Tests core functionality of donation data management
 */
@RunWith(MockitoJUnitRunner.class)
public class DonationRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private SharedPreferencesHelper mockPrefsHelper;

    @Mock
    private Observer<List<Donation>> mockDonationsObserver;

    @Mock
    private Observer<Double> mockTotalObserver;

    @Mock
    private Observer<Integer> mockFamiliesObserver;

    private DonationRepository repository;
    private List<Donation> testDonations;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new DonationRepository(mockPrefsHelper);
        
        // Create test data
        testDonations = createTestDonations();
        
        // Mock SharedPreferences behavior
        when(mockPrefsHelper.getDonations()).thenReturn(testDonations);
    }

    @Test
    public void testGetDonations_ReturnsCorrectData() {
        // Arrange
        repository.getDonations().observeForever(mockDonationsObserver);

        // Act & Assert
        verify(mockDonationsObserver).onChanged(testDonations);
        assertEquals(testDonations.size(), repository.getDonations().getValue().size());
    }

    @Test
    public void testAddDonation_SavesCorrectly() {
        // Arrange
        Donation newDonation = createTestDonation("TEST123", "John Doe", DonationType.CASH, 1000.0);
        
        // Act
        repository.addDonation(newDonation);

        // Assert
        verify(mockPrefsHelper).saveDonations(any(List.class));
    }

    @Test
    public void testGetTotalDonations_CalculatesCorrectly() {
        // Arrange
        repository.getTotalDonations().observeForever(mockTotalObserver);

        // Act & Assert
        double expectedTotal = testDonations.stream()
            .filter(d -> d.getType() == DonationType.CASH)
            .mapToDouble(Donation::getAmount)
            .sum();
        
        verify(mockTotalObserver).onChanged(expectedTotal);
    }

    @Test
    public void testGetTotalFamiliesHelped_CountsCorrectly() {
        // Arrange
        repository.getTotalFamiliesHelped().observeForever(mockFamiliesObserver);

        // Act & Assert
        int expectedFamilies = (int) testDonations.stream()
            .filter(d -> d.getType() == DonationType.GOODS)
            .mapToDouble(Donation::getAmount)
            .sum() / 4; // Assuming 4 items per family
        
        verify(mockFamiliesObserver).onChanged(expectedFamilies);
    }

    @Test
    public void testUpdateDonation_UpdatesCorrectly() {
        // Arrange
        Donation existingDonation = testDonations.get(0);
        existingDonation.setDonorName("Updated Name");

        // Act
        repository.updateDonation(existingDonation);

        // Assert
        verify(mockPrefsHelper).saveDonations(any(List.class));
    }

    @Test
    public void testGetDonationsByType_FiltersCorrectly() {
        // Act
        List<Donation> cashDonations = repository.getDonationsByType(DonationType.CASH);
        List<Donation> goodsDonations = repository.getDonationsByType(DonationType.GOODS);

        // Assert
        assertTrue(cashDonations.stream().allMatch(d -> d.getType() == DonationType.CASH));
        assertTrue(goodsDonations.stream().allMatch(d -> d.getType() == DonationType.GOODS));
    }

    @Test
    public void testGetRecentDonations_LimitsCorrectly() {
        // Act
        List<Donation> recentDonations = repository.getRecentDonations(2);

        // Assert
        assertEquals(2, recentDonations.size());
        // Should be sorted by timestamp descending
        assertTrue(recentDonations.get(0).getTimestamp() >= recentDonations.get(1).getTimestamp());
    }

    @Test
    public void testDeleteDonation_RemovesCorrectly() {
        // Arrange
        String donationId = testDonations.get(0).getId();

        // Act
        repository.deleteDonation(donationId);

        // Assert
        verify(mockPrefsHelper).saveDonations(any(List.class));
    }

    @Test
    public void testGetDonationById_ReturnsCorrectDonation() {
        // Arrange
        String targetId = testDonations.get(0).getId();

        // Act
        Donation result = repository.getDonationById(targetId);

        // Assert
        assertNotNull(result);
        assertEquals(targetId, result.getId());
    }

    @Test
    public void testGetDonationById_ReturnsNullForInvalidId() {
        // Act
        Donation result = repository.getDonationById("INVALID_ID");

        // Assert
        assertNull(result);
    }

    private List<Donation> createTestDonations() {
        List<Donation> donations = new ArrayList<>();
        
        donations.add(createTestDonation("DON001", "Alice Smith", DonationType.CASH, 500.0));
        donations.add(createTestDonation("DON002", "Bob Johnson", DonationType.GOODS, 12.0));
        donations.add(createTestDonation("DON003", "Anonymous", DonationType.CASH, 1000.0));
        donations.add(createTestDonation("DON004", "Carol Brown", DonationType.GOODS, 8.0));
        
        return donations;
    }

    private Donation createTestDonation(String id, String donorName, DonationType type, double amount) {
        Donation donation = new Donation();
        donation.setId(id);
        donation.setDonorName(donorName);
        donation.setType(type);
        donation.setAmount(amount);
        donation.setTimestamp(System.currentTimeMillis());
        donation.setVerified(true);
        
        if (type == DonationType.GOODS) {
            donation.setDescription("Rice: " + (int)(amount / 4) + ", Water: " + (int)(amount / 4) + 
                                  ", Medicine: " + (int)(amount / 4) + ", Clothes: " + (int)(amount / 4));
        } else {
            donation.setDescription("Cash donation for relief operations");
        }
        
        return donation;
    }
}