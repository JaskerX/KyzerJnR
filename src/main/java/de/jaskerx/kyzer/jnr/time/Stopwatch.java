package de.jaskerx.kyzer.jnr.time;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Stopwatch {

	private LocalDateTime start;
	
	/**
	 * Starts the Stopwatch
	 * @return The started Stopwatch
	 */
	public Stopwatch start() {
		start = LocalDateTime.now();
		return this;
	}
	
	/**
	 * Stops the Stopwatch
	 * @return The time measured my the Stopwatch in milliseconds
	 */
	public long stop() {
		LocalDateTime now = LocalDateTime.now();
		return ChronoUnit.MILLIS.between(start, now);
	}
	
}
