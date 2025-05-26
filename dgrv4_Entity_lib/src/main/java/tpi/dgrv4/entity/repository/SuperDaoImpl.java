package tpi.dgrv4.entity.repository;

import java.util.Optional;

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
