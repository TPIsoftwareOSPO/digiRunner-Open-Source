package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpOpenApiKeyMapDao extends JpaRepository<TsmpOpenApiKeyMap, Long> {
	
	public List<TsmpOpenApiKeyMap> findByCreateUser(String createUser);
	
	public int deleteByRefOpenApiKeyId(Long refOpenApiKeyId);
	
	public List<TsmpOpenApiKeyMap> findByRefOpenApiKeyIdAndRefApiUid(Long refOpenApiKeyId, String refApiUid);

	public List<TsmpOpenApiKeyMap> findByRefOpenApiKeyId(Long openApiKeyId);
	
	public TsmpOpenApiKeyMap findFirstByRefOpenApiKeyIdAndRefApiUid(Long refOpenApiKeyId, String refApiUid);
	
}
