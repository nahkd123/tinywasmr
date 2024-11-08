package tinywasmr.extern.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.type.Mutability;

/**
 * <p>
 * Export a field or method as global, memory, table or function in WebAssembly.
 * <ul>
 * <li>Apply this annotation on method to export it as function</li>
 * <li>Apply this annotation on {@link Memory} field to export it as memory
 * (there can be only 1 memory for each instance)</li>
 * <li>Apply this annotation on {@link Table} field to export it as table</li>
 * <li>Apply this annotation on {@link tinywasmr.engine.exec.instance.Function}
 * field to export it as global variable with function reference type
 * (mutability can be explicitly set in this annotation)</li>
 * <li>Apply this annotation on anything else to export it as global variable
 * with external reference type (mutability can be explicitly set in this
 * annotation)</li>
 * </ul>
 * </p>
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Export {
	/**
	 * <p>
	 * The export type. You can explicitly set the export type. For example, if you
	 * want to export your method as {@code funcref} global variable, you use
	 * {@link ExportType#GLOBAL}.
	 * </p>
	 */
	public ExportType value() default ExportType.AUTO;

	/**
	 * <p>
	 * Explicitly export this symbol as different name. If the string is empty
	 * (which is the default), it will automatically pick up the name that was
	 * declared in source code.
	 * </p>
	 */
	public String exportAs() default "";

	/**
	 * <p>
	 * The mutability of the exported field. While the field may be mutable in Java,
	 * the field under WebAssembly view may be immutable, which means the module
	 * can't reassign anything to it.
	 * </p>
	 */
	public Mutability mutability() default Mutability.CONST;
}
