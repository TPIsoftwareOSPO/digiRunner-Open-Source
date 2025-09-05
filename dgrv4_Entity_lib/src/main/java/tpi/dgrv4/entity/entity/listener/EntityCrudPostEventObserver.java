package tpi.dgrv4.entity.entity.listener;

public interface EntityCrudPostEventObserver<T> {

	void postPersist(T entity);

	void postUpdate(T entity);

	void postRemove(T entity);

}
