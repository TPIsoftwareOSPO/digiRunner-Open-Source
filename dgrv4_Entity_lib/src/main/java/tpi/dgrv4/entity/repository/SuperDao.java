package tpi.dgrv4.entity.repository;

import java.util.Optional;

public interface SuperDao<T> {

	/**
	 * Retrieves an entity by its long id.
	 *
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	Optional<T> findByLongId(Long l_id);

	/**
	 * Retrieves an entity by its hex id.
	 *
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	Optional<T> findByHexId(String h_id);

	/**
	 * Returns whether an entity with the given long id exists.
	 *
	 * @param id must not be {@literal null}.
	 * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	boolean existsByLongId(Long l_id);

	/**
	 * Returns whether an entity with the given hex id exists.
	 *
	 * @param id must not be {@literal null}.
	 * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	boolean existsByHexId(String h_id);

	/**
	 * Deletes the entity with the given long id.
	 *
	 * @param id must not be {@literal null}.
	 * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
	 */
	void deleteByLongId(Long l_id);

	/**
	 * Deletes the entity with the given hex id.
	 *
	 * @param id must not be {@literal null}.
	 * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
	 */
	void deleteByHexId(String h_id);

	/**
	 * Saves a given entity and retry when primary key is duplicated. 
	 * Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 *
	 * @param entity must not be {@literal null}.
	 * @return the saved entity; will never be {@literal null}.
	 * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
	 */
	<S extends T> S save(S entity);

	Class<T> getEntityType();

}
