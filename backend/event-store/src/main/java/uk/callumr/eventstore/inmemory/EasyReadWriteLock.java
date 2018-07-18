package uk.callumr.eventstore.inmemory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class EasyReadWriteLock {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public <R> R read(Supplier<R> reader) {
        return withLock(readWriteLock.readLock(), reader);
    }

    public <R> R write(Supplier<R> readerWriter) {
        return withLock(readWriteLock.writeLock(), readerWriter);
    }

    public void write_(Runnable writer) {
        write(() -> {
            writer.run();
            return null;
        });
    }

    private <R> R withLock(Lock lock, Supplier<R> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
}
