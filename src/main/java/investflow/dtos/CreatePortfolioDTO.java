package investflow.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePortfolioDTO(
        @NotBlank
        @Size(min = 2, max = 50)
        String name,

        @NotNull
        @Positive
        Double monthlyAmount,

        @Positive
        int durationMonths
) {}
