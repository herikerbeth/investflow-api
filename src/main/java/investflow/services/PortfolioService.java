package investflow.services;

import investflow.dtos.CreatePortfolioDTO;
import investflow.dtos.ResponsePortfolioDTO;

public interface PortfolioService {

    ResponsePortfolioDTO save(CreatePortfolioDTO dto);

    ResponsePortfolioDTO findById(Integer id);

    Iterable<ResponsePortfolioDTO> findAll();

    void deleteById(Integer id);
}
