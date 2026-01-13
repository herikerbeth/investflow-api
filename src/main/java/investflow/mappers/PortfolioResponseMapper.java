package investflow.mappers;

import investflow.dtos.ResponsePortfolioDTO;
import investflow.models.Portfolio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PortfolioResponseMapper {

    @Mapping(source = "id", target = "id")
    ResponsePortfolioDTO toDTO(Portfolio entity);
}
