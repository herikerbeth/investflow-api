package investflow.exceptions;

public class PortfolioAlreadyExistsException extends RuntimeException {
    public PortfolioAlreadyExistsException(String name) {
        super("Portfolio with name " + name + " already exists");
    }
}
