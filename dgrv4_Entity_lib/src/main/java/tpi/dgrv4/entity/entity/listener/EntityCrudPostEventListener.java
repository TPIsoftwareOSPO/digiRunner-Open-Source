package tpi.dgrv4.entity.entity.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public class EntityCrudPostEventListener {

	private final EntityCrudPostEventObserverRegistry registry;

	public EntityCrudPostEventListener(EntityCrudPostEventObserverRegistry registry) {
		this.registry = registry;
	}

	private <T> void notifyObservers(T entity, ObserverAction<T> action) {

		if (entity == null)
			return;

		try {
			Set<EntityCrudPostEventObserver<?>> observers = registry.getObservers(entity.getClass());

			// 如果觀察者數量少於閾值，使用同步處理
			if (observers.size() <= 3) {
				observers.forEach(observer -> {
					try {
						action.execute((EntityCrudPostEventObserver<T>) observer, entity);
					} catch (Exception e) {
						throw new RuntimeException("Error processing event", e);
					}
				});
				return;
			}

			CountDownLatch latch = new CountDownLatch(observers.size());
			List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

			observers.forEach(observer -> {
				Thread.startVirtualThread(() -> {
					try {
						action.execute((EntityCrudPostEventObserver<T>) observer, entity);
					} catch (Exception e) {
						exceptions.add(e);
					} finally {
						latch.countDown();
					}
				});
			});

			// 添加超時機制
			if (!latch.await(30, TimeUnit.SECONDS)) {
				throw new TimeoutException("Observer notification timeout after 30 seconds");
			}

			// 如果有多個異常，創建 CompositException
			if (!exceptions.isEmpty()) {
				if (exceptions.size() == 1) {
					throw new RuntimeException("Error processing event", exceptions.get(0));
				} else {
					throw new RuntimeException("Multiple errors occurred during event processing",
							new CompositeException(exceptions));
				}
			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Notification process was interrupted", e);
		} catch (Exception e) {
			throw new RuntimeException("Error processing event", e);
		}
	}

	// 用於處理多個異常的輔助類
	private static class CompositeException extends Exception {
		private final List<Exception> exceptions;

		public CompositeException(List<Exception> exceptions) {
			super("Multiple exceptions occurred");
			this.exceptions = new ArrayList<>(exceptions);
		}

		public List<Exception> getExceptions() {
			return Collections.unmodifiableList(exceptions);
		}
	}

	@FunctionalInterface
	private interface ObserverAction<T> {
		void execute(EntityCrudPostEventObserver<T> observer, T entity);
	}

	@PostPersist
	public <T> void postPersist(T entity) {
		notifyObservers(entity, EntityCrudPostEventObserver::postPersist);
	}

	@PostUpdate
	public <T> void postUpdate(T entity) {
		notifyObservers(entity, EntityCrudPostEventObserver::postUpdate);
	}

	@PostRemove
	public <T> void postRemove(T entity) {
		notifyObservers(entity, EntityCrudPostEventObserver::postRemove);
	}

}