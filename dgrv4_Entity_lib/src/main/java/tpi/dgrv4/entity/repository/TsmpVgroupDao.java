package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpVgroup;

@Repository
public interface TsmpVgroupDao extends JpaRepository<TsmpVgroup, String> {

	public List<TsmpVgroup> findByCreateUser(String createUser);
	
	public List<TsmpVgroup> findByAA0229Service(TsmpVgroup lastTsmpVgroup, String securityLevelId, String clientId, String[] words, Integer pageSize);

	public List<TsmpVgroup> query_aa0222Service(TsmpVgroup lastTsmpVgroup, String securityLevelId, List<String> vgroupAuthoritiesIds, String[] words, Integer pageSize);

	public TsmpVgroup findFirstByVgroupIdAndVgroupName( String vgroupId, String vgroupName);

	public List<TsmpVgroup> findByVgroupName( String vgroupName);

	public List<TsmpVgroup> findByVgroupAlias( String vgroupAlias);

	public List<TsmpVgroup> findByVgroupIdAndVgroupName( String vgroupId, String vgroupName);
	
	public List<TsmpVgroup> findByVgroupId(String vgroupId);
	
	public boolean existsByVgroupName(String vgroupName);
	
	public boolean existsByVgroupAliasAndVgroupNameNot(String vgroupAlias, String vgroupName);
	
	public boolean existsByVgroupAlias(String vgroupAlias);
	
	public TsmpVgroup findFirstByVgroupName( String vgroupName);

}
