package investflow.controllers;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.services.PortfolioService;
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
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
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
    public ResponseEntity<ResponsePortfolioDTO> findById(@PathVariable("id") int id) {
        ResponsePortfolioDTO saved = portfolioService.findById(id);

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<Iterable<ResponsePortfolioDTO>> findAll() {
        Iterable<ResponsePortfolioDTO> saved = portfolioService.findAll();

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") int id) {
        portfolioService.deleteById(id);
    }
}