package investflow.controllers;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.exceptions.PortfolioAlreadyExistsException;
import investflow.exceptions.PortfolioNotFoundException;
import investflow.exceptions.handlers.GlobalExceptionHandler;
import investflow.models.Portfolio;
import investflow.services.PortfolioService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@Import(GlobalExceptionHandler.class)
public class PortfolioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    /**
     * CREATE TESTS (POST)
     */

    @Test
    void create_ShouldReturnCreated_WhenValidData() throws Exception {
        CreatePortfolioDTO input = createValidPortfolioDTO();
        ResponsePortfolioDTO response = createResponsePortfolioDTO(createPortfolioEntity(input));

        when(portfolioService.save(any(CreatePortfolioDTO.class))).thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(input);
        String responseJson = objectMapper.writeValueAsString(response);

        mockMvc.perform(post("/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/portfolios/" + response.id()))
                .andExpect(content().json(responseJson));
    }

    @Test
    void create_ShouldReturnConflict_WhenPortfolioAlreadyExists() throws Exception {
        CreatePortfolioDTO input = createValidPortfolioDTO();

        when(portfolioService.save(any(CreatePortfolioDTO.class)))
                .thenThrow(new PortfolioAlreadyExistsException(input.name()));

        String json = objectMapper.writeValueAsString(input);

        mockMvc.perform(post("/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenDataIsInvalid() throws Exception {
        CreatePortfolioDTO invalidInput = new CreatePortfolioDTO("", -1.0, -2);

        String json = objectMapper.writeValueAsString(invalidInput);

        mockMvc.perform(post("/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.durationMonths").exists())
                .andExpect(jsonPath("$.validationErrors.monthlyAmount").exists());

        Mockito.verify(portfolioService, Mockito.never()).save(any());
    }

    /**
     * GET TESTS (FIND)
     */

    @Test
    void findById_ShouldReturnPortfolio_WhenIdExists() throws Exception {
        int id = 1;
        ResponsePortfolioDTO response = createResponsePortfolioDTO(createPortfolioEntity(createValidPortfolioDTO()));

        when(portfolioService.findById(id)).thenReturn(response);

        String responseJson = objectMapper.writeValueAsString(response);

        mockMvc.perform(get("/portfolios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
        int id = 99;
        when(portfolioService.findById(id)).thenThrow(new PortfolioNotFoundException(id));

        mockMvc.perform(get("/portfolios/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void findAll_ShouldReturnList_WhenPortfoliosExist() throws Exception {
        var list = List.of(
                createResponsePortfolioDTO(createPortfolioEntity(createValidPortfolioDTO())),
                createResponsePortfolioDTO(createPortfolioEntity(createAnotherValidPortfolioDTO()))
        );

        when(portfolioService.findAll()).thenReturn(list);

        String json = objectMapper.writeValueAsString(list);

        mockMvc.perform(get("/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(content().json(json));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoPortfoliosExist() throws Exception {
        when(portfolioService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * DELETE TESTS
     */

    @Test
    void delete_ShouldReturnNoContent_WhenIdExists() throws Exception {
        int id = 11;
        doNothing().when(portfolioService).deleteById(id);

        mockMvc.perform(delete("/portfolios/{id}", id))
                .andExpect(status().isNoContent());

        verify(portfolioService, times(1)).deleteById(id);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
        int id = 99;
        doThrow(new PortfolioNotFoundException(id)).when(portfolioService).deleteById(id);

        mockMvc.perform(delete("/portfolios/{id}", id))
                .andExpect(status().isNotFound());
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
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
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