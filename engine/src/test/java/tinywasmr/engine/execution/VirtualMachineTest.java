package tinywasmr.engine.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.execution.stack.StackBackedMachineStack;
import tinywasmr.engine.execution.stack.VoidMachineStack;
import tinywasmr.engine.execution.value.LocalsBuilder;
import tinywasmr.engine.execution.vm.VirtualMachineImpl;
import tinywasmr.engine.instruction.Instructions;
import tinywasmr.engine.io.LEByteBuffer;

class VirtualMachineTest {
	@Test
	void testNop() throws IOException {
		var buffer = new LEByteBuffer(ByteBuffer.wrap(new byte[] {
			Instructions.NOP,
		}));
		var vm = new VirtualMachineImpl();
		vm.execute(buffer, VoidMachineStack.STACK, null);
	}

	@Test
	void testUnreachableTrap() throws IOException {
		var buffer = new LEByteBuffer(ByteBuffer.wrap(new byte[] {
			Instructions.UNREACHABLE,
		}));
		var vm = new VirtualMachineImpl();
		assertThrows(TrapException.class, () -> vm.execute(buffer, VoidMachineStack.STACK, null));
	}

	@Test
	void testAddConst() throws IOException {
		var buffer = new LEByteBuffer(ByteBuffer.wrap(new byte[] {
			Instructions.I32_CONST, 1,
			Instructions.I32_CONST, 1,
			Instructions.I32_ADD,
			Instructions.RETURN,
		}));

		var stack = new StackBackedMachineStack();
		var vm = new VirtualMachineImpl();
		vm.execute(buffer, stack, null);
		assertEquals(2, stack.popI32());
	}

	@Test
	void testAddWithLocals() throws IOException {
		var buffer = new LEByteBuffer(ByteBuffer.wrap(new byte[] {
			Instructions.LOCAL_GET, 0,
			Instructions.LOCAL_GET, 1,
			Instructions.I32_ADD,
			Instructions.RETURN,
		}));

		var stack = new StackBackedMachineStack();
		var vm = new VirtualMachineImpl();
		vm.execute(buffer, stack, new LocalsBuilder()
			.i32(1).i32(2)
			.build());
		assertEquals(1 + 2, stack.popI32());
	}

	@Test
	void testAddWithLocalsComplex() throws IOException {
		var buffer = new LEByteBuffer(ByteBuffer.wrap(new byte[] {
			Instructions.LOCAL_GET, 0,
			Instructions.LOCAL_GET, 1,
			Instructions.I32_ADD,
			Instructions.LOCAL_SET, 3,

			Instructions.LOCAL_GET, 3,
			Instructions.LOCAL_GET, 2,
			Instructions.I32_ADD,

			Instructions.RETURN,
		}));

		var stack = new StackBackedMachineStack();
		var vm = new VirtualMachineImpl();
		vm.execute(buffer, stack, new LocalsBuilder()
			.i32(1).i32(2).i32(3)
			.i32() // Aux
			.build());
		assertEquals(1 + 2 + 3, stack.popI32());
	}
}
