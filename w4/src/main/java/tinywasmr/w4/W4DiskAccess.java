package tinywasmr.w4;

/**
 * <p>
 * An access to persistent storage.
 * </p>
 */
public interface W4DiskAccess {
	void read(byte[] target, int targetOffset, int count);

	void write(byte[] source, int sourceOffset, int count);
}
