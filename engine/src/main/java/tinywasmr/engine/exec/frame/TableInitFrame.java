package tinywasmr.engine.exec.frame;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

public class TableInitFrame extends AbstractFrame {
	private ElementSegment segment;
	private Table table;
	private int segmentOffset;
	private int tableOffset;
	private int count;

	public TableInitFrame(ElementSegment segment, Table table, int segmentOffset, int tableOffset, int count, List<Value> operands, int insn) {
		super(operands, insn);
		this.segment = segment;
		this.table = table;
		this.segmentOffset = segmentOffset;
		this.tableOffset = tableOffset;
		this.count = count;
	}

	public TableInitFrame(ElementSegment segment, Table table, int segmentOffset, int tableOffset, int count) {
		this(segment, table, segmentOffset, tableOffset, count, List.of(), 0);
	}

	public ElementSegment getSegment() { return segment; }

	public Table getTable() { return table; }

	public int getSegmentOffset() { return segmentOffset; }

	public int getTableOffset() { return tableOffset; }

	public int getCount() { return count; }

	@Override
	public List<Instruction> getExecutingInsns() { return Collections.emptyList(); }

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(List.of()); }
}
