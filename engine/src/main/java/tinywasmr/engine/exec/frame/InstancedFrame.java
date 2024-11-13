package tinywasmr.engine.exec.frame;

import tinywasmr.engine.exec.instance.Instance;

public interface InstancedFrame extends Frame {
	Instance getInstance();
}
