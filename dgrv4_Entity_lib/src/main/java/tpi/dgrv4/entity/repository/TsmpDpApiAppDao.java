package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpApiApp;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAppId;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpDpApiAppDao extends JpaRepository<TsmpDpApiApp, TsmpDpApiAppId> {

	public List<TsmpDpApiApp> findAllByRefAppId(Long refAppId);

	public long deleteByRefAppId(Long refAppId);

	public long countByRefAppId(Long refAppId);

	public List<TsmpDpApiApp> findByCreateUser(String createUser);

}
