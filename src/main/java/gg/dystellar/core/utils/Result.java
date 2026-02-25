package gg.dystellar.core.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class Result<R, E> implements Supplier<R> {

	private final R res;
	private final E err;

	private Result(@Nullable R result, @Nullable E error) {
		this.res = result;
		this.err = error;
	}

	public static <R, E> Result<R, E> ok(final R result) {
		return new Result<>(result, null);
	}

	public static <R, E> Result<R, E> err(final E error) {
		return new Result<>(null, error);
	}

	@Override
	public R get() {
		return this.res;
	}
	
	public boolean isOk() {
		return this.res != null;
	}

	public boolean isErr() {
		return this.err != null;
	}

	public Result<R, E> ifOk(Consumer<R> func) {
		if (res != null) func.accept(this.res);
		return this;
	}

	public Result<R, E> ifErr(Consumer<E> func) {
		if (this.err != null) func.accept(this.err);
		return this;
	}
}
