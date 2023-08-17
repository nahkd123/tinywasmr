package tinywasmr.trace;

import picocli.CommandLine;

public class Main {
	public static void main(String[] args) {
		System.exit(new CommandLine(new MainCommand()).execute(args));
	}
}
