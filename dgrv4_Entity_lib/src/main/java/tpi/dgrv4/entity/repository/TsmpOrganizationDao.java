package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpOrganization;

@Repository
public interface TsmpOrganizationDao extends JpaRepository<TsmpOrganization, String> {

	// 找出包含此 orgId 及其向下的所有組織
	public List<String> queryOrgDescendingByOrgId_rtn_id(String orgId, Integer pageSize);

	// 找出包含此 orgId 及其向下的所有組織
	public List<TsmpOrganization> queryOrgDescendingByOrgId_rtn_entity(String orgId, Integer pageSize);

	public List<TsmpOrganization> findByCreateUser(String createUser);
	
	public List<String> findByOrgName(String orgName, Integer pageSize);
	
	public List<TsmpOrganization> findByOrgPathStartsWith(String orgPath);
	
	public List<TsmpOrganization> findByOrgIdAndOrgName(String orgId, String orgName);
	
	public TsmpOrganization findFirstByParentId(String parentId);
	
	public TsmpOrganization findByOrgName(String orgName);
	public TsmpOrganization findFirstByParentIdIsNullOrParentIdIs(String parentId);

}
