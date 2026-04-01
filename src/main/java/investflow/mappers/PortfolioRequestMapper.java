package investflow.mappers;

import investflow.dtos.CreatePortfolioDTO;
import investflow.models.Portfolio;

public class PortfolioRequestMapper {

    private PortfolioRequestMapper() {}

    public static Portfolio toEntity(CreatePortfolioDTO dto) {
        return Portfolio.builder()
                .name(dto.name())
                .monthlyAmount(dto.monthlyAmount())
                .durationMonths(dto.durationMonths())
                .build();
    }
}
