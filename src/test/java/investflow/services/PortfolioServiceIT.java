package investflow.services;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.exceptions.PortfolioAlreadyExistsException;
import investflow.exceptions.PortfolioNotFoundException;
import investflow.mappers.PortfolioResponseMapper;
import investflow.models.Portfolio;
import investflow.repositories.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class PortfolioServiceIT {

    @Container
    @ServiceConnection
    static MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0").withReuse(false);

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioResponseMapper portfolioResponseMapper;

    @BeforeEach
    void cleanDatabase() {
        portfolioRepository.deleteAll();
    }

    @Test
    void shouldCreatePortfolio_whenValidDataIsProvided() {
        // Arrange
        CreatePortfolioDTO portfolioToCreate = createValidPortfolioDTO();

        // Act
        ResponsePortfolioDTO createdPortfolio = portfolioService.save(portfolioToCreate);

        // Assert
        assertThat(createdPortfolio).isNotNull();
        assertThat(createdPortfolio.id()).isNotNull();

        assertThat(portfolioRepository.findById(createdPortfolio.id()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(portfolioToCreate);
    }

    @Test
    void shouldThrowException_whenPortfolioNameAlreadyExists() {
        CreatePortfolioDTO existingPortfolioData = createValidPortfolioDTO();
        portfolioService.save(existingPortfolioData);

        assertThatThrownBy(() -> portfolioService.save(existingPortfolioData))
                .isInstanceOf(PortfolioAlreadyExistsException.class)
                .hasMessageContaining(existingPortfolioData.name());
    }

    @Test
    void shouldReturnPortfolioById_whenPortfolioExists() {
        // Arrange
        Portfolio entityToSave = createPortfolioEntity(createValidPortfolioDTO());
        Portfolio persistedPortfolio = portfolioRepository.save(entityToSave);
        ResponsePortfolioDTO expectedResponse =  portfolioResponseMapper.toDTO(persistedPortfolio);

        // Act
        ResponsePortfolioDTO actualResponse = portfolioService.findById(persistedPortfolio.getId());

        // Assert
        assertThat(actualResponse)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowException_WhenPortfolioDoesNotExist() {
        int nonExistentPortfolioId = 999;

        assertThatThrownBy(() -> portfolioService.findById(nonExistentPortfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio Not Found")
                .hasMessageContaining(String.valueOf(nonExistentPortfolioId));
    }

    @Test
    void shouldReturnAllPortfolios_whenPortfolioExists() {
        // Arrange
        Portfolio portfolioToSave1 = createPortfolioEntity(createValidPortfolioDTO());
        Portfolio portfolioToSave2 = createPortfolioEntity(createAnotherValidPortfolioDTO());

        List<Portfolio> persistedPortfolios = portfolioRepository.saveAll(List.of(portfolioToSave1, portfolioToSave2));

        ResponsePortfolioDTO expectedResponse1 = portfolioResponseMapper.toDTO(persistedPortfolios.get(0));
        ResponsePortfolioDTO expectedResponse2 = portfolioResponseMapper.toDTO(persistedPortfolios.get(1));

        // Act
        Iterable<ResponsePortfolioDTO> actualResponse = portfolioService.findAll();

        // Assert
        assertThat(actualResponse)
                .isNotEmpty()
                .hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expectedResponse1, expectedResponse2);
    }

    @Test
    void shouldReturnEmptyList_whenNoPortfoliosExist() {
        Iterable<ResponsePortfolioDTO> actualResponse = portfolioService.findAll();

        assertThat(actualResponse).isEmpty();
    }

    @Test
    void shouldRemovePortfolioFromDatabase_whenIdIsValid() {
        // Arrange
        Portfolio entityToSave = createPortfolioEntity(createValidPortfolioDTO());
        Portfolio persistedPortfolio = portfolioRepository.save(entityToSave);

        // Act
        portfolioService.deleteById(persistedPortfolio.getId());

        // Assert
        assertThat(portfolioRepository.findById(persistedPortfolio.getId()))
                .isEmpty();
    }

    @Test
    void shouldThrowException_whenPortfolioDoesNotExist() {
        int nonExistentPortfolioId = 999;

        assertThatThrownBy(() -> portfolioService.deleteById(nonExistentPortfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio Not Found")
                .hasMessageContaining(String.valueOf(nonExistentPortfolioId));
    }

    private CreatePortfolioDTO createValidPortfolioDTO() {
        return new CreatePortfolioDTO("Conservative Portfolio", 500.0, 12);
    }

    private CreatePortfolioDTO createAnotherValidPortfolioDTO() {
        return new CreatePortfolioDTO("Aggressive Portfolio", 1500.0, 36);
    }

    private Portfolio createPortfolioEntity(CreatePortfolioDTO dto) {
        return Portfolio.builder()
                .name(dto.name())
                .monthlyAmount(dto.monthlyAmount())
                .durationMonths(dto.durationMonths())
                .build();
    }
}
