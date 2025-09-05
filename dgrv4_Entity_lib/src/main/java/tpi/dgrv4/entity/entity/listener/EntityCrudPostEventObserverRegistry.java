package tpi.dgrv4.entity.entity.listener;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

@Component
public class EntityCrudPostEventObserverRegistry {

	private final Map<Class<?>, Set<EntityCrudPostEventObserver<?>>> observerMap = new ConcurrentHashMap<>();
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();
	private final Lock writeLock = rwLock.writeLock();

	// 註冊 observer
	public <T> void register(Class<T> entityType, EntityCrudPostEventObserver<T> observer) {

		if (entityType == null || observer == null) {
			return;
		}

		writeLock.lock();
		try {
			observerMap.computeIfAbsent(entityType, k -> ConcurrentHashMap.newKeySet())
					.add((EntityCrudPostEventObserver<?>) observer);
		} finally {
			writeLock.unlock();
		}
	}

	// 移除 observer
	public <T> void unregister(Class<T> entityType, EntityCrudPostEventObserver<T> observer) {
		writeLock.lock();
		try {
			observerMap.compute(entityType, (key, observers) -> {
				if (observers == null) {
					return null;
				}
				observers.remove(observer);
				return observers.isEmpty() ? null : observers;
			});
		} finally {
			writeLock.unlock();
		}
	}

	// 檢查是否存在 observer
	public <T> boolean hasObservers(Class<T> entityType) {
		readLock.lock();
		try {
			Set<EntityCrudPostEventObserver<?>> observers = observerMap.get(entityType);
			return observers != null && !observers.isEmpty();
		} finally {
			readLock.unlock();
		}
	}

	public <T> boolean containsObserver(Class<T> entityType, EntityCrudPostEventObserver<T> observer) {
		if (entityType == null || observer == null) {
			throw new IllegalArgumentException("EntityType and observer must not be null");
		}
		readLock.lock();
		try {
			Set<EntityCrudPostEventObserver<?>> observers = observerMap.get(entityType);
			return observers != null && observers.contains(observer);
		} finally {
			readLock.unlock();
		}
	}

	public void clearAllObservers() {
		writeLock.lock();
		try {
			observerMap.clear();
		} finally {
			writeLock.unlock();
		}
	}

	public Map<Class<?>, Set<EntityCrudPostEventObserver<?>>> getObserverMap() {
		readLock.lock();
		try {
			return Collections.unmodifiableMap(this.observerMap);
		} finally {
			readLock.unlock();
		}
	}

	public Set<EntityCrudPostEventObserver<?>> getObservers(Class<?> entityType) {
		readLock.lock();
		try {
			return Collections.unmodifiableSet(observerMap.getOrDefault(entityType, Collections.emptySet()));
		} finally {
			readLock.unlock();
		}
	}
}