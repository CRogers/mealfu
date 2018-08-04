package uk.callumr.eventstore.core.internal;

import java.util.Iterator;

public class SwitchingIterator<T> implements Iterator<T> {
    private Iterator<T> backingIterator;

    public SwitchingIterator(Iterator<T> backingIterator) {
        setBackingIterator(backingIterator);
    }

    public void setBackingIterator(Iterator<T> backingIterator) {
        this.backingIterator = backingIterator;
    }

    @Override
    public boolean hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public T next() {
        return backingIterator.next();
    }
}
