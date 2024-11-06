package tinywasmr.engine.exec.vm;

import java.util.List;
import java.util.Stack;

import tinywasmr.engine.exec.frame.ExternalFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.trap.Trap;

public class DefaultMachine implements Machine {
	private Trap trap = null;
	private Stack<Frame> frames;
	private Stack<FunctionFrame> functions;
	private ExternalFrame extern;
	private boolean runtimeValidation;

	public DefaultMachine(List<Frame> frames, boolean runtimeValidation) {
		if (!(frames.get(0) instanceof ExternalFrame extern))
			throw new IllegalArgumentException("The first frame is not ExternalFrame");
		this.frames = new Stack<>();
		this.functions = new Stack<>();
		this.extern = extern;
		for (Frame frame : frames) pushFrame(frame);
		this.runtimeValidation = runtimeValidation;
	}

	public DefaultMachine() {
		this(List.of(new ExternalFrame()), false);
	}

	@Override
	public Stack<Frame> getFrameStack() { return frames; }

	@Override
	public ExternalFrame getExternalFrame() { return extern; }

	@Override
	public Frame peekFrame() {
		return frames.peek();
	}

	@Override
	public FunctionFrame peekFunctionFrame() {
		return functions.peek();
	}

	@Override
	public void pushFrame(Frame frame) {
		frames.push(frame);
		if (frame instanceof FunctionFrame functionFrame) functions.push(functionFrame);
	}

	@Override
	public Frame popFrame() {
		if (frames.peek() == extern) throw new IllegalStateException("Can't pop ExternalFrame");
		Frame out = frames.pop();
		if (out == functions.peek()) functions.pop();
		return out;
	}

	@Override
	public Trap getTrap() { return trap; }

	@Override
	public void setTrap(Trap trap) { this.trap = trap; }

	@Override
	public boolean hasRuntimeValidation() {
		return runtimeValidation;
	}

	@Override
	public void setRuntimeValidation(boolean validation) { runtimeValidation = validation; }
}
