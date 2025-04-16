package io.github.sibmaks.spring.jfr;

import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author sibmaks
 * @since 0.0.16
 */
@AllArgsConstructor
public class JavaFlightRecorderObjectRegistry {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Object, String> objectToId = new ConcurrentHashMap<>();
    private final Map<String, Object> idToObject = new ConcurrentHashMap<>();

    /**
     * Register object in registry
     *
     * @param object object instance
     * @return object identifier
     */
    public String registerObject(Object object) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            var objectId = objectToId.get(object);
            if (objectId != null) {
                return objectId;
            }
        } finally {
            readLock.unlock();
        }

        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            var objectId = objectToId.get(object);
            if (objectId != null) {
                return objectId;
            }
            objectId = UUID.randomUUID().toString();
            objectToId.put(object, objectId);
            idToObject.put(objectId, object);
            return objectId;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Register object in registry with proposal identifier
     *
     * @param object   object instance
     * @param proposal proposal identifier
     * @return actual object identifier
     */
    public String registerObject(Object object, String proposal) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            var objectId = objectToId.get(object);
            if (objectId != null && objectId.equals(proposal)) {
                return objectId;
            }
        } finally {
            readLock.unlock();
        }

        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            var existed = idToObject.get(proposal);
            var objectId = proposal;
            if (existed != null && existed != object) {
                objectId = UUID.randomUUID().toString();
            }
            var existedId = objectToId.get(object);
            if (existedId != null && !existedId.equals(objectId)) {
                throw new IllegalStateException("Object already registered with id: " + existedId);
            }
            objectToId.put(object, objectId);
            idToObject.put(objectId, object);
            return objectId;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Remove object from registry and return true if removed, false otherwise.
     *
     * @param object object to remove from registry
     * @return true if removed, false otherwise
     */
    public boolean remove(Object object) {
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            var objectId = objectToId.remove(object);
            if (objectId != null) {
                idToObject.remove(objectId);
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }
}
