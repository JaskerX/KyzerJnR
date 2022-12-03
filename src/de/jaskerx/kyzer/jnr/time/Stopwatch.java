package de.jaskerx.kyzer.jnr.time;

public class Stopwatch {

	private long start;
	
	/**
	 * Starts the Stopwatch
	 * @return Stopwatch - The started Stopwatch
	 */
	public Stopwatch start() {
		start = System.nanoTime();
		return this;
	}
	
	/**
	 * Stops the Stopwatch
	 * @return long - The time measured my the Stopwatch in nanoseconds
	 */
	public long stop() {
		long now = System.nanoTime();
		return now - start;
	}
	
}
