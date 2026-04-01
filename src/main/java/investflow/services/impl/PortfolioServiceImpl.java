package investflow.services.impl;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;
import investflow.exceptions.PortfolioAlreadyExistsException;
import investflow.exceptions.PortfolioNotFoundException;
import investflow.mappers.PortfolioRequestMapper;
import investflow.mappers.PortfolioResponseMapper;
import investflow.models.Portfolio;
import investflow.repositories.PortfolioRepository;
import investflow.services.PortfolioService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;

    private final PortfolioResponseMapper responseMapper;

    @Override
    @Transactional
    public ResponsePortfolioDTO save(@NotNull CreatePortfolioDTO dto) {
        if (portfolioRepository.existsByName(dto.name())) {
            throw new PortfolioAlreadyExistsException(dto.name());
        }

        Portfolio savedEntity = PortfolioRequestMapper.toEntity(dto);
        Portfolio portfolio = portfolioRepository.save(savedEntity);

        return responseMapper.toDTO(portfolio);
    }

    @Override
    public ResponsePortfolioDTO findById(Integer id) {
        return portfolioRepository.findById(id).map(responseMapper::toDTO)
                .orElseThrow(() -> new PortfolioNotFoundException(id));
    }

    @Override
    public Iterable<ResponsePortfolioDTO> findAll() {
        List<Portfolio> portfolios = portfolioRepository.findAll();

        return portfolios.stream()
                .map(responseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!portfolioRepository.existsById(id)) {
            throw new PortfolioNotFoundException(id);
        }
        portfolioRepository.deleteById(id);
    }
}
