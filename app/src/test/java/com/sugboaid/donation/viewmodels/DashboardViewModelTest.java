package com.sugboaid.donation.viewmodels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.sugboaid.donation.viewmodels.DashboardViewModel;
import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.repositories.InventoryRepository;
import com.sugboaid.repositories.PreferencesRepository;

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
 * Unit tests for DashboardViewModel
 * Tests dashboard statistics and data presentation logic
 */
@RunWith(MockitoJUnitRunner.class)
public class DashboardViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DonationRepository mockDonationRepository;

    @Mock
    private InventoryRepository mockInventoryRepository;

    @Mock
    private PreferencesRepository mockPreferencesRepository;

    @Mock
    private Observer<List<Donation>> mockRecentDonationsObserver;

    @Mock
    private Observer<Double> mockTotalDonationsObserver;

    @Mock
    private Observer<Integer> mockFamiliesHelpedObserver;

    @Mock
    private Observer<Integer> mockDistributedItemsObserver;

    private DashboardViewModel viewModel;
    private MutableLiveData<List<Donation>> donationsLiveData;
    private MutableLiveData<Double> totalDonationsLiveData;
    private MutableLiveData<Integer> familiesHelpedLiveData;
    private MutableLiveData<Integer> distributedItemsLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize LiveData objects
        donationsLiveData = new MutableLiveData<>();
        totalDonationsLiveData = new MutableLiveData<>();
        familiesHelpedLiveData = new MutableLiveData<>();
        distributedItemsLiveData = new MutableLiveData<>();

        // Mock repository responses
        when(mockDonationRepository.getDonations()).thenReturn(donationsLiveData);
        when(mockDonationRepository.getTotalDonations()).thenReturn(totalDonationsLiveData);
        when(mockDonationRepository.getTotalFamiliesHelped()).thenReturn(familiesHelpedLiveData);
        when(mockInventoryRepository.getTotalDistributedItems()).thenReturn(distributedItemsLiveData);

        viewModel = new DashboardViewModel(mockDonationRepository, mockInventoryRepository, mockPreferencesRepository);
    }

    @Test
    public void testGetRecentDonations_ReturnsCorrectData() {
        // Arrange
        List<Donation> testDonations = createTestDonations();
        viewModel.getRecentDonations().observeForever(mockRecentDonationsObserver);

        // Act
        donationsLiveData.setValue(testDonations);

        // Assert
        verify(mockRecentDonationsObserver).onChanged(any(List.class));
    }

    @Test
    public void testGetTotalDonations_ReturnsCorrectValue() {
        // Arrange
        double expectedTotal = 2500.0;
        viewModel.getTotalDonations().observeForever(mockTotalDonationsObserver);

        // Act
        totalDonationsLiveData.setValue(expectedTotal);

        // Assert
        verify(mockTotalDonationsObserver).onChanged(expectedTotal);
    }

    @Test
    public void testGetFamiliesHelped_ReturnsCorrectCount() {
        // Arrange
        int expectedFamilies = 15;
        viewModel.getFamiliesHelped().observeForever(mockFamiliesHelpedObserver);

        // Act
        familiesHelpedLiveData.setValue(expectedFamilies);

        // Assert
        verify(mockFamiliesHelpedObserver).onChanged(expectedFamilies);
    }

    @Test
    public void testGetDistributedItems_ReturnsCorrectCount() {
        // Arrange
        int expectedItems = 120;
        viewModel.getDistributedItems().observeForever(mockDistributedItemsObserver);

        // Act
        distributedItemsLiveData.setValue(expectedItems);

        // Assert
        verify(mockDistributedItemsObserver).onChanged(expectedItems);
    }

    @Test
    public void testRefreshData_CallsRepositoryMethods() {
        // Act
        viewModel.refreshData();

        // Assert
        verify(mockDonationRepository).refreshData();
        verify(mockInventoryRepository).refreshData();
    }

    @Test
    public void testCalculatePercentageChange_PositiveChange() {
        // Act
        double result = viewModel.calculatePercentageChange(100.0, 120.0);

        // Assert
        assertEquals(20.0, result, 0.01);
    }

    @Test
    public void testCalculatePercentageChange_NegativeChange() {
        // Act
        double result = viewModel.calculatePercentageChange(100.0, 80.0);

        // Assert
        assertEquals(-20.0, result, 0.01);
    }

    @Test
    public void testCalculatePercentageChange_ZeroPrevious() {
        // Act
        double result = viewModel.calculatePercentageChange(0.0, 50.0);

        // Assert
        assertEquals(100.0, result, 0.01);
    }

    @Test
    public void testFormatCurrency_CorrectFormat() {
        // Act
        String result = viewModel.formatCurrency(1234.56);

        // Assert
        assertEquals("₱1,234.56", result);
    }

    @Test
    public void testFormatPercentage_PositiveValue() {
        // Act
        String result = viewModel.formatPercentage(15.5);

        // Assert
        assertEquals("+15.5%", result);
    }

    @Test
    public void testFormatPercentage_NegativeValue() {
        // Act
        String result = viewModel.formatPercentage(-10.2);

        // Assert
        assertEquals("-10.2%", result);
    }

    @Test
    public void testGetStatisticsData_ReturnsCompleteData() {
        // Arrange
        totalDonationsLiveData.setValue(5000.0);
        familiesHelpedLiveData.setValue(25);
        distributedItemsLiveData.setValue(200);

        // Act
        DashboardViewModel.StatisticsData stats = viewModel.getStatisticsData();

        // Assert
        assertNotNull(stats);
        assertEquals(5000.0, stats.getTotalDonations(), 0.01);
        assertEquals(25, stats.getFamiliesHelped());
        assertEquals(200, stats.getDistributedItems());
    }

    @Test
    public void testIsDataLoading_InitiallyTrue() {
        // Assert
        assertTrue(viewModel.isDataLoading().getValue());
    }

    @Test
    public void testIsDataLoading_FalseAfterDataLoad() {
        // Act
        totalDonationsLiveData.setValue(1000.0);
        familiesHelpedLiveData.setValue(10);
        distributedItemsLiveData.setValue(50);

        // Assert
        assertFalse(viewModel.isDataLoading().getValue());
    }

    private List<Donation> createTestDonations() {
        List<Donation> donations = new ArrayList<>();
        
        Donation donation1 = new Donation();
        donation1.setId("DON001");
        donation1.setDonorName("Test Donor 1");
        donation1.setType(DonationType.CASH);
        donation1.setAmount(1000.0);
        donation1.setTimestamp(System.currentTimeMillis());
        donations.add(donation1);

        Donation donation2 = new Donation();
        donation2.setId("DON002");
        donation2.setDonorName("Test Donor 2");
        donation2.setType(DonationType.GOODS);
        donation2.setAmount(20.0);
        donation2.setTimestamp(System.currentTimeMillis() - 1000);
        donations.add(donation2);

        return donations;
    }
}