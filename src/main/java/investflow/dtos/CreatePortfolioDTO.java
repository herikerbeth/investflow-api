package investflow.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for creating a new portfolio")
public record CreatePortfolioDTO(

        @Schema(
                description = "Portfolio name",
                example = "Brazilian Stocks",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2,
                maxLength = 50)
        @NotBlank
        @Size(min=2, max=50)
        String name,

        @Schema(
                description = "Monthly investment amount",
                example = "1000.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        Double monthlyAmount,

        @Schema(
                description = "Investment duration in months",
                example = "12",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Positive
        int durationMonths
) {}
