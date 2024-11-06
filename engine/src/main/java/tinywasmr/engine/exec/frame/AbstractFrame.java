package tinywasmr.engine.exec.frame;

import java.util.List;
import java.util.Stack;

import tinywasmr.engine.exec.value.Value;

public abstract class AbstractFrame implements Frame {
	private Stack<Value> operands;
	private int insn;

	public AbstractFrame(List<Value> operands, int insn) {
		this.insn = insn;
		this.operands = new Stack<>();
		this.operands.addAll(operands);
	}

	@Override
	public Stack<Value> getOperandStack() { return operands; }

	@Override
	public void pushOperand(Value value) {
		operands.push(value);
	}

	@Override
	public Value popOprand() {
		return operands.pop();
	}

	@Override
	public Value peekOperand() {
		return operands.peek();
	}

	@Override
	public int getInsnIndex() { return insn; }

	@Override
	public void setInsnIndex(int index) { this.insn = index; }

	@Override
	public void incInsnIndex() {
		insn++;
	}

	@Override
	public void incInsnIndex(int count) {
		insn += count;
	}
}
