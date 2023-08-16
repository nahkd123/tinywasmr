package tinywasmr.engine.util;

public class HexString {
	private static final char[] HEX = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String ofByte(byte b) {
		var v = ((int) b) & 0xFF;
		return String.valueOf(new char[] { HEX[(v & 0xF0) >> 4], HEX[v & 0x0F] });
	}

	public static String ofBytes(byte[] bs) {
		var s = "";
		for (int i = 0; i < bs.length; i++) s += ofByte(bs[i]);
		return s;
	}
}
