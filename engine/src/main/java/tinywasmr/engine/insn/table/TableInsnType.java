package tinywasmr.engine.insn.table;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;

public enum TableInsnType {
	GET {
		@Override
		public void execute(Machine vm, Table table) {
			Value index = vm.peekFrame().popOprand();
			vm.peekFrame().pushOperand(table.get(index.i32()));
		}
	},
	SET {
		@Override
		public void execute(Machine vm, Table table) {
			Value val = vm.peekFrame().popOprand();
			Value index = vm.peekFrame().popOprand();
			if (!(val instanceof RefValue refValue))
				throw new ValidationException("Not reference type: %s".formatted(val.type()));
			table.set(index.i32(), refValue);
		}
	},
	GROW {
		@Override
		public void execute(Machine vm, Table table) {
			Value filler = vm.peekFrame().popOprand();
			Value delta = vm.peekFrame().popOprand();
			if (!(filler instanceof RefValue refValue))
				throw new ValidationException("Not reference type: %s".formatted(filler.type()));
			int prevSize = table.grow(delta.i32(), refValue);
			vm.peekFrame().pushOperand(new NumberI32Value(prevSize));
		}
	},
	SIZE {
		@Override
		public void execute(Machine vm, Table table) {
			vm.peekFrame().pushOperand(new NumberI32Value(table.size()));
		}
	},
	FILL {
		@Override
		public void execute(Machine vm, Table table) {
			Value count = vm.peekFrame().popOprand();
			Value filler = vm.peekFrame().popOprand();
			if (!(filler instanceof RefValue refValue))
				throw new ValidationException("Not reference type: %s".formatted(filler.type()));
			Value offset = vm.peekFrame().popOprand();
			table.fill(offset.i32(), refValue, count.i32());
		}
	};

	public abstract void execute(Machine vm, Table table);
}
