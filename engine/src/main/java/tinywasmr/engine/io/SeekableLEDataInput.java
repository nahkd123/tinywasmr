package tinywasmr.engine.io;

public interface SeekableLEDataInput extends LEDataInput {
	public long getPosition();

	public void seekTo(long position);
}
