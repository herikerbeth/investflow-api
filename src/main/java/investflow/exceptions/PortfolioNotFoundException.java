package investflow.exceptions;

public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(Integer id) {
        super("Portfolio Not Found: " + id);
    }
}
