package tinywasmr.engine.module.table;

import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.type.TableType;

/**
 * <p>
 * Represent the table declaration. Table declaration can be used to obtain the
 * {@link Table} from {@link Instance}.
 * </p>
 */
public interface TableDecl {
	TableType type();
}
