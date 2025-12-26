package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;

@Repository
public interface TsmpSecurityLevelDao extends JpaRepository<TsmpSecurityLevel, String> {
	
	public TsmpSecurityLevel findFirstBySecurityLevelIdAndSecurityLevelName(String securityLevelId, String securityLevelName);
	
	public TsmpSecurityLevel findFirstBySecurityLevelName(String securityLevelName);
	
	//AA1116
	public List<TsmpSecurityLevel> querySecurityLevelList(TsmpSecurityLevel lastRecord, String[] words, int pageSize);
	
	public boolean existsBySecurityLevelId(String securityLevelId);
	
	public boolean existsBySecurityLevelNameAndSecurityLevelIdNot(String securityLevelName, String securityLevelId);
	
	public boolean existsBySecurityLevelName(String securityLevelName);

}
