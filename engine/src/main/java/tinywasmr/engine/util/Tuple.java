package tinywasmr.engine.util;

public final class Tuple {
	public static final class Duo<A, B> {
		private A a;
		private B b;

		public Duo(A a, B b) {
			this.a = a;
			this.b = b;
		}

		public A getA() { return a; }

		public B getB() { return b; }

		@Override
		public String toString() {
			return "Duo[" + a + ", " + b + "]";
		}
	}
}
