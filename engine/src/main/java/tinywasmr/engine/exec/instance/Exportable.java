package tinywasmr.engine.exec.instance;

/**
 * <p>
 * Base type for all exportable stuffs like function, table, memory and global.
 * </p>
 */
public interface Exportable {
	Instance instance();
}
