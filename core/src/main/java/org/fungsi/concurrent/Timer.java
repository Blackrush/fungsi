package org.fungsi.concurrent;

import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

import java.time.Duration;

import static org.fungsi.Unit.unit;

public interface Timer {
    <T> Future<T> flatSchedule(Duration duration, UnsafeSupplier<Future<T>> fn);

	<T> Future<T> schedule(Duration interval, UnsafeSupplier<T> fn);

	default Future<Unit> schedule(Duration interval, UnsafeRunnable fn) {
		return schedule(interval, fn.thenReturn(unit()));
	}
}
