package org.fungsi.concurrent;

import com.google.common.collect.ImmutableList;
import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeFunction;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Futures {
	private Futures() {}

	public static Future<Unit> unit() {
		return Future.constant(Unit.left());
	}

	public static <T> Future<T> success(T value) {
		return Future.constant(Either.success(value));
	}

	public static <T> Future<T> failure(Throwable cause) {
		return Future.constant(Either.failure(cause));
	}

	public static <T> Future<T> flatten(Either<Future<T>, Throwable> e) {
		return e.fold(Function.identity(), Futures::failure);
	}

	public static <T, R> Function<T, Future<R>> safe(UnsafeFunction<T, Future<R>> fn) {
		return fn.safeFunction().andThen(Futures::flatten);
	}

    @SuppressWarnings("unchecked")
	public static <T> Future<List<T>> collect(List<Future<T>> futures) {
        if (futures.isEmpty()) {
            return Futures.success(ImmutableList.of());
        }

        if (futures.size() == 1) {
            return futures.get(0).map(ImmutableList::of);
        }

        final Promise<List<T>> promise = Promises.create();

        final Object[] lock = new Object[0];
        final Object[] result = new Object[futures.size()];
        final int[] index = {0};

        for (Future<T> future : futures) {
            future
            .onFailure(promise::fail)
            .onSuccess(res -> {
                if (promise.isDone()) return;

                synchronized (lock) {
                    result[index[0]++] = res;

                    if (index[0] >= result.length) {
                        promise.complete(Arrays.asList((T[]) result));
                    }
                }
            })
            ;
        }

        return promise;
	}

	public static <T> Collector<Future<T>, ?, Future<List<T>>> collect() {
        return Collectors.collectingAndThen(Collectors.toList(), Futures::collect);
	}
}
