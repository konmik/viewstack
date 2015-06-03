package viewstack.util;

/**
 * This class allows to apply some basic OOP patterns:
 * 1. A constructor should be code-free.
 * 2. A field should not expose null value.
 * 3. Lazy initialization.
 * 4. Cache.
 *
 * @param <T> a type of value to hold
 */
public class Lazy<T> {

    public interface Factory<T> {
        T call();
    }

    private Factory<T> factory;
    private T cache;
    private boolean initialized;

    public Lazy(Factory<T> factory) {
        this.factory = factory;
    }

    public T get() {
        if (!initialized) {
            cache = factory.call();
            initialized = true;
        }
        return cache;
    }

    public void invalidate() {
        cache = null;
        initialized = false;
    }
}
