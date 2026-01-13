package investflow.dtos;

import java.time.LocalDate;

public record ResponsePortfolioDTO(
        Integer id,
        String name,
        Double monthlyAmount,
        int durationMonths,
        LocalDate createdAt,
        LocalDate updatedAt
) {}
