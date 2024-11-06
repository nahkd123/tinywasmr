package tinywasmr.engine.exec.trap;

/**
 * <p>
 * The trap is generated from external function/execution, like throwing
 * {@link Throwable} for example.
 * </p>
 */
public record ExternalTrap(Throwable throwable) implements Trap {
}
