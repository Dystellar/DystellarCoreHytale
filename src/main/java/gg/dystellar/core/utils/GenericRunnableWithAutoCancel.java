package net.zylesh.dystellarcore.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public interface GenericRunnableWithAutoCancel<E> {
    void run(E object, AtomicBoolean isFinished);
}
