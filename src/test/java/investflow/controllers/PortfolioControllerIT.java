package investflow.controllers;

import investflow.dtos.ApiError;
import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.models.Portfolio;
import investflow.repositories.PortfolioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestRestTemplate
public class PortfolioControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0").withReuse(false);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PortfolioRepository portfolioRepository;

    // Cleans up the database after each test to ensure isolation
    @AfterEach
    void tearDown() {
        portfolioRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a portfolio successfully and return 201 Created")
    void shouldCreatePortfolioSuccessfully() {
        // Arrange
        CreatePortfolioDTO request = createValidPortfolioDTO();

        // Act
        ResponseEntity<ResponsePortfolioDTO> response = restTemplate.postForEntity(
                "/portfolios", request, ResponsePortfolioDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo(request.name());

        // Verify if the Location header is correctly populated
        URI location = response.getHeaders().getLocation();
        assertThat(location).isNotNull();
        assertThat(location.getPath()).endsWith("/portfolios/" + response.getBody().id());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestWhenValidationFails() {
        // Arrange: Invalid data (empty name, negative amount, negative duration)
        CreatePortfolioDTO request = new CreatePortfolioDTO("", -100.0, -5);

        // Act
        ResponseEntity<ApiError> response = restTemplate.postForEntity(
                "/portfolios", request, ApiError.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().validationErrors())
                .containsKeys("name", "monthlyAmount", "durationMonths");
    }

    @Test
    @DisplayName("Should return 409 Conflict when portfolio name already exists")
    void shouldReturnConflictWhenPortfolioNameAlreadyExists() {
        // Arrange
        CreatePortfolioDTO request = createValidPortfolioDTO();

        // Save successfully for the first time
        restTemplate.postForEntity("/portfolios", request, ResponsePortfolioDTO.class);

        // Act: Attempt to save again with the same name
        ResponseEntity<ApiError> response = restTemplate.postForEntity(
                "/portfolios", request, ApiError.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).contains(request.name());
    }

    @Test
    @DisplayName("Should find a portfolio by ID and return 200 OK")
    void shouldFindPortfolioByIdAndReturnOk() {
        // Arrange: Create and save an entity directly to the database for the test
        Portfolio entity = createPortfolioEntity(createValidPortfolioDTO());
        Portfolio savedPortfolio = portfolioRepository.save(entity);

        // Act: Perform HTTP GET call to the controller route
        ResponseEntity<ResponsePortfolioDTO> response = restTemplate.getForEntity(
                "/portfolios/" + savedPortfolio.getId(),
                ResponsePortfolioDTO.class
        );

        // Assert: Validate HTTP status and if data is mapped correctly
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(savedPortfolio.getId());
        assertThat(response.getBody().name()).isEqualTo(savedPortfolio.getName());
        assertThat(response.getBody().monthlyAmount()).isEqualTo(savedPortfolio.getMonthlyAmount());
    }

    @Test
    @DisplayName("Should return 404 Not Found when searching for a non-existent ID")
    void shouldReturnNotFoundWhenIdDoesNotExist() {
        // Arrange: Use an ID that we know doesn't exist (database is cleaned after each test)
        int nonExistentId = 9999;

        // Act: Perform HTTP GET call expecting the error structure
        ResponseEntity<ApiError> response = restTemplate.getForEntity(
                "/portfolios/" + nonExistentId,
                ApiError.class
        );

        // Assert: Validate if GlobalExceptionHandler caught and returned the correct ApiError
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().path()).isEqualTo("/portfolios/" + nonExistentId);
    }

    @Test
    @DisplayName("Should find all portfolios and return 200 OK with the list of items")
    void shouldFindAllPortfoliosAndReturnOk() {
        // Arrange: Prepare and save two distinct entities in the database
        Portfolio portfolio1 = createPortfolioEntity(createValidPortfolioDTO());
        Portfolio portfolio2 = createPortfolioEntity(createAnotherValidPortfolioDTO());
        portfolioRepository.saveAll(List.of(portfolio1, portfolio2));

        // Act: Perform GET request mapping the response to a DTO array
        ResponseEntity<ResponsePortfolioDTO[]> response = restTemplate.getForEntity(
                "/portfolios",
                ResponsePortfolioDTO[].class
        );

        // Assert: Validate status and list content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        // Extract names from response to validate if the correct data was returned
        List<String> returnedNames = Arrays.stream(response.getBody())
                .map(ResponsePortfolioDTO::name)
                .toList();

        assertThat(returnedNames).containsExactlyInAnyOrder(
                "Conservative Portfolio",
                "Aggressive Portfolio"
        );
    }

    @Test
    @DisplayName("Should return 200 OK with an empty list when there are no records")
    void shouldReturnEmptyListWhenNoPortfoliosExist() {
        // Act
        ResponseEntity<ResponsePortfolioDTO[]> response = restTemplate.getForEntity(
                "/portfolios",
                ResponsePortfolioDTO[].class
        );

        // Assert: Validate that API doesn't break and returns empty array (proper REST behavior)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should delete an existing portfolio and return 204 No Content")
    void shouldDeletePortfolioSuccessfully() {
        // Arrange: Prepare and save real data in the database
        Portfolio entity = createPortfolioEntity(createValidPortfolioDTO());
        Portfolio savedPortfolio = portfolioRepository.save(entity);

        // Act: Use the exchange method to trigger DELETE and capture the response
        ResponseEntity<Void> response = restTemplate.exchange(
                "/portfolios/" + savedPortfolio.getId(),
                HttpMethod.DELETE,
                null, // No request body
                Void.class // Expecting an empty response (No Content)
        );

        // Assert: Validate HTTP status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Assert: Physically ensure the record is no longer in the database
        assertThat(portfolioRepository.existsById(savedPortfolio.getId())).isFalse();
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to delete a non-existent ID")
    void shouldReturnNotFoundWhenDeletingNonExistentId() {
        // Arrange: An ID that is definitely not in the database (thanks to @AfterEach)
        int nonExistentId = 9999;

        // Act: Make the request mapping the possible error response to your ApiError class
        ResponseEntity<ApiError> response = restTemplate.exchange(
                "/portfolios/" + nonExistentId,
                HttpMethod.DELETE,
                null,
                ApiError.class
        );

        // Assert: Validate if GlobalExceptionHandler processed the error properly
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains(String.valueOf(nonExistentId));
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