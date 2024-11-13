package tinywasmr.engine.exec.frame.init;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.AbstractFrame;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

/**
 * <p>
 * Initialize an entry of element segment to table.
 * </p>
 */
public class InitElementFrame extends AbstractFrame {
	private ElementSegment segment;
	private int segmentOffset;
	private Table table;
	private int tableOffset;
	private int index;

	public InitElementFrame(ElementSegment segment, int segmentOffset, Table table, int tableOffset, int index, List<Value> operands, int step) {
		super(operands, step);
		this.segment = segment;
		this.segmentOffset = segmentOffset;
		this.table = table;
		this.tableOffset = tableOffset;
		this.index = index;
	}

	public InitElementFrame(ElementSegment segment, int segmentOffset, Table table, int tableOffset, int index) {
		this(segment, segmentOffset, table, tableOffset, index, Collections.emptyList(), 0);
	}

	public ElementSegment getSegment() { return segment; }

	public Table getTable() { return table; }

	/**
	 * <p>
	 * Offset in segment.
	 * </p>
	 */
	public int getSegmentOffset() { return segmentOffset; }

	/**
	 * <p>
	 * Offset in table.
	 * </p>
	 */
	public int getTableOffset() { return tableOffset; }

	/**
	 * <p>
	 * Segment entry index.
	 * </p>
	 */
	public int getIndex() { return index; }

	@Override
	public boolean isFrameFinished() { return getStep() >= segment.inits().get(index).size() + 1; }

	@Override
	public void branchThis() {
		setStep(segment.inits().get(index).size());
	}

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(Collections.emptyList()); }

	@Override
	public void executeStep(Machine vm) {
		if (isFrameFinished()) return;
		if (getStep() == segment.inits().get(index).size()) {
			Value val = popOprand();
			if (!(val instanceof RefValue ref)) throw new ValidationException("Type is not reftype: %s".formatted(val));
			table.set(tableOffset + index, ref);
		} else {
			segment.inits().get(index).get(getStep()).execute(vm);
		}
	}
}
