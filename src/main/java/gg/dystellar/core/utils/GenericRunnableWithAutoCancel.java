package gg.dystellar.core.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public interface GenericRunnableWithAutoCancel<E> {
    void run(E object, AtomicBoolean isFinished);
}
