package investflow.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Response DTO containing complete portfolio data")
public record ResponsePortfolioDTO(

        @Schema(
                description = "Unique portfolio ID",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,

        @Schema(
                description = "Portfolio name",
                example = "Brazilian Stocks",
                minLength = 2,
                maxLength = 50)
        String name,

        @Schema(
                description = "Monthly investment amount",
                example = "1000.00")
        Double monthlyAmount,

        @Schema(
                description = "Total investment duration in months",
                example = "12")
        int durationMonths,

        @Schema(
                description = "Record creation date",
                example = "2026-01-1",
                format = "date",
                accessMode = Schema.AccessMode.READ_ONLY)
        LocalDate createdAt,

        @Schema(
                description = "Record last update date",
                example = "2026-12-31",
                format = "date",
                accessMode = Schema.AccessMode.READ_ONLY)
        LocalDate updatedAt
) {}
