package tpi.dgrv4.entity.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeqAssignResult;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeqEntityHelper;

public abstract class SuperDaoImpl<T> implements SuperDao<T> {

	private final static String REPO_SUFFIX = "$$base.dao";

	@PersistenceContext
	private EntityManager entityManager;

	private ConfigurableListableBeanFactory beanFactory;

	@Autowired
	public void setSuperDaoImpl(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Optional<T> findByLongId(Long l_id) {
		return getRepository().findById(l_id);
	}

	@Override
	public Optional<T> findByHexId(String h_id) {
		Long l_id = RandomSeqLongUtil.toLongValue(h_id);
		return findByLongId(l_id);
	}

	@Override
	public boolean existsByLongId(Long l_id) {
		return getRepository().existsById(l_id);
	}

	@Override
	public boolean existsByHexId(String h_id) {
		Long l_id = RandomSeqLongUtil.toLongValue(h_id);
		return existsByLongId(l_id);
	}

	@Override
	public void deleteByLongId(Long l_id) {
		getRepository().deleteById(l_id);
	}

	@Override
	public void deleteByHexId(String h_id) {
		Long l_id = RandomSeqLongUtil.toLongValue(h_id);
		deleteByLongId(l_id);
	}

	// 新增, 更新
	@SuppressWarnings("rawtypes")
	@Override
	public <S extends T> S save(S entity) {
		// 取號
		DgrSeqAssignResult result = DgrSeqEntityHelper.assignDgrSeq(entity, false);
		if (!result.hasDgrSeqAlready()) {
			boolean isIdExists = getRepository().existsById(result.getDgrSeq());
			if (isIdExists) {
				// ID已存在就重新取號
				result = DgrSeqEntityHelper.assignDgrSeq(entity, true);
			}
		}
		return getRepository().save(entity);
	}

	// 批次新增, 更新
	@SuppressWarnings("rawtypes")
	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
		if (entities == null) {
			throw new IllegalArgumentException("The given Iterable of entities must not be null!");
		}

		List<S> entityList = new ArrayList<>();
		List<Long> assignedIds = new ArrayList<>();

		// 步驟 1: 批次分配 ID
		for (S entity : entities) {
			DgrSeqAssignResult result = DgrSeqEntityHelper.assignDgrSeq(entity, false);
			entityList.add(entity);

			// 收集新分配的 ID
			if (!result.hasDgrSeqAlready()) {
				assignedIds.add(result.getDgrSeq());
			}
		}

		// 步驟 2: 批次檢查 ID 是否重複
		if (!assignedIds.isEmpty()) {
			Set<Long> duplicateIds = new HashSet<>();
			for (Long id : assignedIds) {
				if (getRepository().existsById(id)) {
					duplicateIds.add(id);
				}
			}

			// 步驟 3: 為重複的 ID 重新取號
			if (!duplicateIds.isEmpty()) {
				for (S entity : entityList) {
					@SuppressWarnings("rawtypes")
					DgrSeqAssignResult result = new DgrSeqAssignResult(entity);
					Long currentId = result.getDgrSeq();

					if (duplicateIds.contains(currentId)) {
						// 重新取號直到不重複
						DgrSeqAssignResult newResult;
						do {
							newResult = DgrSeqEntityHelper.assignDgrSeq(entity, true);
						} while (getRepository().existsById(newResult.getDgrSeq()) ||
								duplicateIds.contains(newResult.getDgrSeq()));

						// 更新已分配的 ID 集合
						duplicateIds.remove(currentId);
						duplicateIds.add(newResult.getDgrSeq());
					}
				}
			}
		}

		// 步驟 4: 一次性批次儲存
		return getRepository().saveAll(entityList);
	}

	@SuppressWarnings("unchecked")
	private CrudRepository<T, Long> getRepository() {
		String repoName = getRepositoryBeanName();
		boolean isRepoExists = this.beanFactory.containsBean(repoName);
		if (!isRepoExists) {
			// Create and registry a repository bean
			synchronized (this.beanFactory) {
				isRepoExists = this.beanFactory.containsBean(repoName);
				if (!isRepoExists) {
					Class<T> domainClass = getEntityType();
					SimpleJpaRepository<T, Long> jpaRepository;
					jpaRepository = new SimpleJpaRepository<T, Long>(domainClass, entityManager);

					this.beanFactory.registerSingleton(repoName, jpaRepository);
				}
			}
		}
		return this.beanFactory.getBean(repoName, CrudRepository.class);
	}

	private String getRepositoryBeanName() {
		Class<T> entityType = getEntityType();
		String beanName = entityType.getName() + REPO_SUFFIX;
		return beanName;
	}

}
