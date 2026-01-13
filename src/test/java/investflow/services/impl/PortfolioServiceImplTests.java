package investflow.services.impl;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.exceptions.PortfolioAlreadyExistsException;
import investflow.exceptions.PortfolioNotFoundException;
import investflow.mappers.PortfolioResponseMapper;
import investflow.models.Portfolio;
import investflow.repositories.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceImplTests {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioResponseMapper responseMapper;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @Test
    void shouldCreatePortfolio_WhenNameIsUnique() {
        // Arrange
        Portfolio portfolioToSave = createPortfolioEntity(createValidPortfolioDTO());
        ResponsePortfolioDTO expectedPortfolio = createResponsePortfolioDTO(portfolioToSave);

        when(portfolioRepository.existsByName((anyString()))).thenReturn(false);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolioToSave);
        when(responseMapper.toDTO(portfolioToSave)).thenReturn(expectedPortfolio);

        // Act
        ResponsePortfolioDTO actualPortfolio = portfolioService.save(createValidPortfolioDTO());

        // Assertion
        assertThat(actualPortfolio)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(expectedPortfolio);

        verify(portfolioRepository).save(any(Portfolio.class));
        verify(responseMapper).toDTO(portfolioToSave);
    }

    @Test
    void shouldRejectPortfolioCreation_whenNameAlreadyExists() {
        CreatePortfolioDTO portfolioWithExistingName = createValidPortfolioDTO();

        when(portfolioRepository.existsByName((portfolioWithExistingName.name()))).thenReturn(true);

        assertThatThrownBy(() -> portfolioService.save(portfolioWithExistingName))
                .isInstanceOf(PortfolioAlreadyExistsException.class)
                .hasMessage("Portfolio Name Already Exists: " + portfolioWithExistingName.name());

        verify(portfolioRepository).existsByName(portfolioWithExistingName.name());
        verify(portfolioRepository, never()).save(any());
        verifyNoInteractions(responseMapper);
    }

    @Test
    void shouldReturnPortfolio_WhenPortfolioExists() {
        // Arrange
        int portfolioId = 1;
        Portfolio storedPortfolio = createPortfolioEntity(createValidPortfolioDTO());
        ResponsePortfolioDTO expectedPortfolio = createResponsePortfolioDTO(storedPortfolio);

        when(responseMapper.toDTO(storedPortfolio)).thenReturn(expectedPortfolio);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(storedPortfolio));

        // Act
        ResponsePortfolioDTO actualPortfolio = portfolioService.findById(portfolioId);

        // Assertion
        assertThat(actualPortfolio)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(expectedPortfolio);

        verify(portfolioRepository).findById(portfolioId);
        verify(responseMapper).toDTO(storedPortfolio);
    }

    @Test
    void shouldFailToFindPortfolio_whenPortfolioDoesNotExist() {
        int unknownPortfolioId = 999;

        when(portfolioRepository.findById(unknownPortfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.findById(unknownPortfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio Not Found: " + unknownPortfolioId);

        verify(portfolioRepository).findById(unknownPortfolioId);
        verifyNoInteractions(responseMapper);
    }

    @Test
    void shouldReturnAllPortfolios_WhenPortfoliosExist() {
        // Arrange
        Portfolio firstPortfolio = createPortfolioEntity(createValidPortfolioDTO());
        ResponsePortfolioDTO firstExpectedPortfolio = createResponsePortfolioDTO(firstPortfolio);

        Portfolio secondPortfolio = createPortfolioEntity(createAnotherValidPortfolioDTO());
        ResponsePortfolioDTO secondExpectedPortfolio = createResponsePortfolioDTO(secondPortfolio);

        List<Portfolio> storedPortfolios = List.of(firstPortfolio, secondPortfolio);

        when(portfolioRepository.findAll()).thenReturn(storedPortfolios);
        when(responseMapper.toDTO(firstPortfolio)).thenReturn(firstExpectedPortfolio);
        when(responseMapper.toDTO(secondPortfolio)).thenReturn(secondExpectedPortfolio);

        // Act
        Iterable<ResponsePortfolioDTO> actualPortfolios = portfolioService.findAll();

        // Assertion
        assertThat(actualPortfolios)
                .isNotEmpty()
                .hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(firstExpectedPortfolio, secondExpectedPortfolio);

        verify(portfolioRepository).findAll();
        verify(responseMapper, times(2)).toDTO(any(Portfolio.class));
    }

    @Test
    void shouldReturnNoPortfolios_whenNoneExist() {
        when(portfolioRepository.findAll()).thenReturn(Collections.emptyList());

        Iterable<ResponsePortfolioDTO> actualPortfolios = portfolioService.findAll();

        assertThat(actualPortfolios).isEmpty();

        verify(portfolioRepository).findAll();
        verifyNoInteractions(responseMapper);
    }

    @Test
    void shouldDeletePortfolio_WhenPortfolioExists() {
        // Arrange
        int existingPortfolioId = 1;

        when(portfolioRepository.existsById(existingPortfolioId)).thenReturn(true);

        // Act
        portfolioService.deleteById(existingPortfolioId);

        // Assert
        verify(portfolioRepository).existsById(existingPortfolioId);
        verify(portfolioRepository).deleteById(existingPortfolioId);
        verifyNoMoreInteractions(portfolioRepository);
    }

    @Test
    void shouldThrowException_whenDeletingNonExistentPortfolio() {
        int missingPortfolioId = 999;
        when(portfolioRepository.existsById(missingPortfolioId)).thenReturn(false);

        assertThatThrownBy(() -> portfolioService.deleteById(missingPortfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio Not Found: " + missingPortfolioId);

        verify(portfolioRepository).existsById(missingPortfolioId);
        verify(portfolioRepository, never()).deleteById(anyInt());
    }

    private CreatePortfolioDTO createValidPortfolioDTO() {
        return new CreatePortfolioDTO("Conservative Portfolio", 500.0, 12);
    }

    private CreatePortfolioDTO createAnotherValidPortfolioDTO() {
        return new CreatePortfolioDTO("Aggressive Portfolio", 1500.0, 36);
    }

    private Portfolio createPortfolioEntity(CreatePortfolioDTO dto) {
        return Portfolio.builder()
                .id(1)
                .name(dto.name())
                .monthlyAmount(dto.monthlyAmount())
                .durationMonths(dto.durationMonths())
                .build();
    }

    private ResponsePortfolioDTO createResponsePortfolioDTO(Portfolio entity) {
        return new ResponsePortfolioDTO(
                entity.getId(),
                entity.getName(),
                entity.getMonthlyAmount(),
                entity.getDurationMonths(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
