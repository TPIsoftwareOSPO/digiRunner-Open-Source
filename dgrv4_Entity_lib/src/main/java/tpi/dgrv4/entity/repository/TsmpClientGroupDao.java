package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientGroupId;

@Repository
public interface TsmpClientGroupDao extends JpaRepository<TsmpClientGroup, TsmpClientGroupId> {

	public long deleteByGroupId(String groupId);
	
	public List<TsmpClientGroup> findByClientId(String clientId);

	public List<TsmpClientGroup> findByGroupId(String groupId);

	/** 使用clientId，找出 groupId 與TsmpGroup 資料表對應的groupId，而且是不包含 輸入securityLV */
	public List<TsmpClientGroup> queryGroupIdNotInTsmpGroupBySecurityLV(String clientId, String securityLV);

	public long deleteByClientId(String clientId);

	public long deleteByClientIdAndGroupId(String clientId, String groupId);
	
	public long countByClientId(String clientId);

}