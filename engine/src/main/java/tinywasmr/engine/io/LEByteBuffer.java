package tinywasmr.engine.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LEByteBuffer implements SeekableLEDataInput {
	private ByteBuffer backed;

	public LEByteBuffer(ByteBuffer backed) {
		this.backed = backed;
	}

	@Override
	public int readByte() throws IOException {
		if (backed.position() >= backed.capacity()) return -1;
		return ((int) backed.get()) & 0xFF;
	}

	@Override
	public long getPosition() { return backed.position(); }

	@Override
	public void seekTo(long position) {
		backed.position((int) position);
	}
}
