package investflow.controllers;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.services.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolios", description = "Endpoints for investment portfolio management")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    @Operation(summary = "Create a new portfolio",
            description = "Create a new portfolio with the provided data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Portfolio successfully created",
                    content = @Content(schema = @Schema(implementation = ResponsePortfolioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Portfolio already exists", content = @Content)
    })
    public ResponseEntity<ResponsePortfolioDTO> create(@RequestBody @Valid CreatePortfolioDTO request) {
        ResponsePortfolioDTO saved = portfolioService.save(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find portfolio by ID",
            description = "Returns a specific portfolio based on its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio found",
                    content = @Content(schema = @Schema(implementation = ResponsePortfolioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found", content = @Content)
    })
    public ResponseEntity<ResponsePortfolioDTO> findById(@PathVariable("id") int id) {
        ResponsePortfolioDTO saved = portfolioService.findById(id);

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(
            summary = "List all portfolios",
            description = "Returns a list of all portfolios"
    )
    @ApiResponse(responseCode = "200", description = "List successfully retrieved",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ResponsePortfolioDTO.class))
            )
    )
    public ResponseEntity<Iterable<ResponsePortfolioDTO>> findAll() {
        Iterable<ResponsePortfolioDTO> saved = portfolioService.findAll();

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete portfolio",
            description = "Removes a portfolio from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Portfolio successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Portfolio ID to be deleted", example = "1", required = true)
            @PathVariable("id") int id) {
        portfolioService.deleteById(id);
    }
}