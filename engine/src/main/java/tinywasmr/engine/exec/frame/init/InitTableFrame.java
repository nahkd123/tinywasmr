package tinywasmr.engine.exec.frame.init;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.frame.AbstractFrame;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

public class InitTableFrame extends AbstractFrame {
	private ElementSegment segment;
	private int segmentOffset;
	private Table table;
	private int tableOffset;
	private int count;

	public InitTableFrame(ElementSegment segment, int segmentOffset, Table table, int tableOffset, int count, List<Value> operands, int step) {
		super(operands, step);
		this.segment = segment;
		this.segmentOffset = segmentOffset;
		this.table = table;
		this.tableOffset = tableOffset;
		this.count = count;
	}

	public InitTableFrame(ElementSegment segment, int segmentOffset, Table table, int tableOffset, int count) {
		this(segment, segmentOffset, table, tableOffset, count, Collections.emptyList(), 0);
	}

	public ElementSegment getSegment() { return segment; }

	public Table getTable() { return table; }

	public int getSegmentOffset() { return segmentOffset; }

	public int getTableOffset() { return tableOffset; }

	public int getCount() { return count; }

	@Override
	public boolean isFrameFinished() { return getStep() >= count; }

	@Override
	public void branchThis() {
		throw new IllegalCallerException("Cannot branch table init frame");
	}

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(Collections.emptyList()); }

	@Override
	public void executeStep(Machine vm) {
		if (isFrameFinished()) return;
		vm.pushFrame(new InitElementFrame(segment, segmentOffset, table, tableOffset, getStep()));
	}
}
