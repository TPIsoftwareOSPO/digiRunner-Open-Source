package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpClientVgroupId;

@Repository
public interface TsmpClientVgroupDao extends JpaRepository<TsmpClientVgroup, TsmpClientVgroupId> {
	public List<TsmpClientVgroup> findByClientId(String clientId);
	
	/** 使用clientId，找出 groupId 與TsmpGroup 資料表對應的groupId，而且是不包含 輸入securityLV*/
	public List<TsmpClientVgroup> queryGroupIdNotInTsmpVGroupBySecurityLV(String clientId, String securityLV);

	public long deleteByClientId(String clientId);

	public List<TsmpClientVgroup> findByVgroupIdAndClientId(String vgroupId, String clientId);

	public List<TsmpClientVgroup> findByVgroupId(String vgroupId);

}
