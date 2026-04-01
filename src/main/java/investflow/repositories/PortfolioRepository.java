package investflow.repositories;

import investflow.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
    boolean existsByName(String name);
}
