package investflow.exceptions;

public class PortfolioAlreadyExistsException extends RuntimeException {
    public PortfolioAlreadyExistsException(String name) {
        super("Portfolio Name Already Exists: " + name);
    }
}
