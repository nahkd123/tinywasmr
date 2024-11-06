package tinywasmr.engine.module.func;

import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.type.FunctionType;

/**
 * <p>
 * Represent a function declaration. In order to call the function, the module
 * need to be instantiated to {@link Instance}, and then you get the function
 * from {@link Instance#functions()}.
 * </p>
 * <p>
 * The implementation of this function declaration interface must be
 * <em>unique</em>, such that {@link #hashCode()} will always be unique every
 * time a new declaration is created, and {@link #equals(Object)} will only
 * return {@code true} if the references are the same (basically 2 pointers
 * points to same memory location in C, or in Java, 2 objects are equals when
 * using {@code ==} operator, or in JavaScript, each declaration is an unique
 * symbol).
 * </p>
 */
public interface FunctionDecl {
	FunctionType type();
}
