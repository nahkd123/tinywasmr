package tinywasmr.w4;

import java.util.function.Consumer;

import tinywasmr.dbg.AutoDebugSymbols;
import tinywasmr.dbg.DebugInterface;
import tinywasmr.dbg.DebugStepMode;
import tinywasmr.dbg.DebugSymbols;
import tinywasmr.engine.exec.StepResult;
import tinywasmr.engine.exec.executor.DefaultExecutor;
import tinywasmr.engine.exec.executor.Executor;
import tinywasmr.engine.exec.instance.DefaultInstance;
import tinywasmr.engine.exec.instance.Export;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.instance.SimpleImporter;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.DefaultMachine;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.extern.ReflectedInstance;
import tinywasmr.extern.ReflectedModule;

/**
 * <p>
 * WASM-4 console implementation.
 * </p>
 */
public class W4 implements DebugInterface {
	private static final ReflectedModule<W4Environment> ENV_MODULE = new ReflectedModule<>(W4Environment.class);
	private Machine machine;
	private Executor executor;
	private ReflectedInstance<W4Environment> env;
	private Instance cart;
	private W4ConsoleState state;

	// Debugger
	private boolean running = true;
	private int executionSpeed = -1;
	private AutoDebugSymbols symbols = new AutoDebugSymbols();
	private long lastExec = -1L;

	public W4(Machine machine, Executor executor, ReflectedInstance<W4Environment> env, Instance cart, W4ConsoleState state) {
		this.machine = machine;
		this.executor = executor;
		this.env = env;
		this.cart = cart;
		this.state = state;

		symbols.setName(ENV_MODULE, "wasm4");
		symbols.setName(cart.module(), "cartridge");
	}

	public static W4 create(Machine machine, Executor executor, WasmModule module, W4DiskAccess disk, Consumer<String> tracer) {
		ReflectedInstance<W4Environment> env = ENV_MODULE.instanceOf(new W4Environment(disk, tracer));
		DefaultInstance cart = new DefaultInstance(module, SimpleImporter.builder().module("env", env).build());
		W4 w4 = new W4(machine, executor, env, cart, W4ConsoleState.INIT);
		w4.getFramebuffer().setPaletteRGB(0, 0xe0f8cf);
		w4.getFramebuffer().setPaletteRGB(1, 0x86c06c);
		w4.getFramebuffer().setPaletteRGB(2, 0x306850);
		w4.getFramebuffer().setPaletteRGB(3, 0x071821);
		w4.env.object().getMemory()[W4Framebuffer.DRAW_COLORS + 0] = 0x03;
		w4.env.object().getMemory()[W4Framebuffer.DRAW_COLORS + 1] = 0x12;
		w4.getInput().setMouseX(0x7fff);
		w4.getInput().setMouseY(0x7fff);
		return w4;
	}

	public static W4 create(WasmModule module, W4DiskAccess disk, Consumer<String> tracer) {
		return create(new DefaultMachine(), new DefaultExecutor(), module, disk, tracer);
	}

	@Override
	public Machine getMachine() { return machine; }

	public ReflectedInstance<W4Environment> getEnv() { return env; }

	public Instance getCart() { return cart; }

	public W4Framebuffer getFramebuffer() { return env.object().getFramebuffer(); }

	public W4Input getInput() { return env.object().getInput(); }

	@Override
	public DebugSymbols getSymbols() { return symbols; }

	@Override
	public boolean isRunning() { return running; }

	@Override
	public void pause() {
		running = false;
	}

	@Override
	public void resume() {
		running = true;
	}

	@Override
	public void step(DebugStepMode mode) {
		if (running) return;

		if (machine.getTrap() != null) {
			printTrap();
			return;
		}

		switch (mode) {
		case IN:
			stepIn();
			return;
		case NEXT: {
			int level = machine.getFrameStack().size();
			do stepIn(); while (machine.getTrap() == null && machine.getFrameStack().size() > level);
			return;
		}
		case OUT: {
			int level = machine.getFrameStack().size();
			do stepIn(); while (machine.getTrap() == null && machine.getFrameStack().size() >= level);
			return;
		}
		default:
			return;
		}
	}

	public void update() {
		long now = System.nanoTime();

		if (lastExec == -1L) {
			lastExec = now;

			if (executionSpeed == -1) {
				if (!running) return;
				do stepIn(); while (running && machine.peekFrame() != machine.getExternalFrame());
			}

			return;
		}

		if (executionSpeed == -1) {
			lastExec = now;
			if (!running) return;
			do stepIn(); while (running && machine.peekFrame() != machine.getExternalFrame());
		} else {
			// amount of time that hasn't executed anything
			long deltaExec = now - lastExec;
			long insnsLeft = executionSpeed * deltaExec / 1000000000L;

			if (!running) {
				lastExec = now;
				return;
			}

			if (insnsLeft > 0L) {
				lastExec = now;

				do {
					stepIn();
					insnsLeft--;
				} while (running && insnsLeft > 0L && machine.peekFrame() != machine.getExternalFrame());
			}
		}
	}

	private void stepIn() {
		if (machine.getTrap() != null) {
			running = false;
			return;
		}

		StepResult result = machine.peekFrame() == machine.getExternalFrame() ? stepInHost() : executor.step(machine);

		if (result == StepResult.TRAP) {
			printTrap();
			running = false;
		}
	}

	private void printTrap() {
		if (machine.getTrap() instanceof ExternalTrap extern) extern.throwable().printStackTrace();
		System.err.println("trap");
	}

	/**
	 * <p>
	 * Called when the control is belong to host (the current frame is external
	 * frame).
	 * </p>
	 */
	private StepResult stepInHost() {
		switch (state) {
		case INIT:
			machine.call(cart.initFunction(), new Value[0]);
			state = W4ConsoleState.START;
			return StepResult.NORMAL;
		case START: {
			Export start = cart.export("start");
			if (start != null) machine.call(start.asFunction(), new Value[0]);
			state = W4ConsoleState.UPDATE;
			return StepResult.NORMAL;
		}
		case UPDATE:
			if ((env.object().getSystemFlags() & W4Environment.SYSTEM_PRESERVE_FB) == 0) getFramebuffer().clear();
			machine.call(cart.export("update").asFunction(), new Value[0]);
			return StepResult.NORMAL;
		default:
			machine.setTrap(new ExternalTrap(new RuntimeException("Unknown state: %s".formatted(state))));
			return StepResult.TRAP;
		}
	}

	@Override
	public int getExecutionSpeed() { return executionSpeed; }

	@Override
	public void setExecutionSpeed(int speed) { executionSpeed = speed; }
}
