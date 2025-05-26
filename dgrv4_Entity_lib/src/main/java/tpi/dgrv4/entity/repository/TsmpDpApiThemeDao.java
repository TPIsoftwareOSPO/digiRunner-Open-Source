package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiThemeId;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpDpApiThemeDao extends JpaRepository<TsmpDpApiTheme, TsmpDpApiThemeId> {

	public List<TsmpDpApiTheme> findAllByRefApiThemeId(Long refApiThemeId);

	public List<TsmpDpApiTheme> findAllByRefApiUid(String refApiUid);

	public long deleteByRefApiThemeId(Long refApiThemeId);

	public long countByRefApiThemeId(Long refApiThemeId);

	public long deleteByRefApiUid(String refApiUid);
	
	public List<TsmpDpApiTheme> findByCreateUser(String createUser);

}