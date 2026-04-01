package investflow.exceptions;

public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(Integer id) {
        super("Portfolio with id " + id + " not found");
    }
}
