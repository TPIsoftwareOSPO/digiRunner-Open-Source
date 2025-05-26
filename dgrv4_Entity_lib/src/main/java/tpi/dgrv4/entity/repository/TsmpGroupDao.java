package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.entity.entity.TsmpGroup;

import java.util.List;

@Repository
public interface TsmpGroupDao extends JpaRepository<TsmpGroup, String> {

	public List<TsmpGroup> findByVgroupFlagAndAliasAndCreateNameFromDpApp(String vgroupFlag, String groupAlias,
			String createUser, boolean isBoundApp, Long appId);

	public List<TsmpGroup> findByVgroupName(String VgroupName);

	public TsmpGroup findFirstByGroupName(String groupName);

	public List<TsmpGroup> findByCreateUser(String createUser);

	public TsmpGroup findFirstByGroupIdAndVgroupFlag(String groupId, String vgroupFlag);

	public List<TsmpGroup> findByGroupName(String groupName);

	public List<TsmpGroup> query_aa0228Service(String groupName, String groupId, String securityLevelID,
			String clientID, String vGroupFlag, String[] words, Integer pageSize);

	/**
	 * 找出擁有某個模組的API, 且擁有與群組名稱相同名稱的有效用戶的群組資料
	 * 
	 * @param moduleName
	 * @param regStatus
	 * @return
	 */
	public List<TsmpGroup> query_SyncTsmpdpapiToDpClient(String moduleName, String regStatus);

	public List<TsmpGroup> findByGroupAlias(String groupName);

	public List<TsmpGroup> findByVgroupIdAndVgroupFlag(String vgroupId, String vgroupFlag);

	public List<TsmpGroup> findByGroupNameAndVgroupFlag(String groupName, String vgroupFlag);

	public List<TsmpGroup> findByGroupAliasAndVgroupFlag(String groupAlias, String vgroupFlag);

	public List<TsmpGroup> queryByAA0238Service(String groupId, List<String> groupAuthoritieIdList,
			String securityLevelId, String[] words, Integer pageSize);

	public List<TsmpGroup> query_AA0303Service_01(String apiKey, String moduleName);

	public long deleteByGroupIdAndGroupName(String groupId, String groupName);

	public long deleteByGroupId(String groupId);

	public List<TsmpGroup> findBySecurityLevelId(String securityLevelId);

	public boolean existsByGroupNameAndVgroupFlag(String groupName, String vgroupFlag);

	public boolean existsByGroupAliasAndVgroupFlagAndGroupNameNot(String groupAlias, String vgroupFlag,
			String groupName);

	public boolean existsByGroupAliasAndVgroupFlag(String groupAlias, String vgroupFlag);

	public TsmpGroup findFirstByGroupNameAndVgroupFlag(String groupName, String vgroupFlag);

	public List<TsmpGroup> findByGroupIdContainingOrGroupNameContainingOrGroupAliasContaining(String groupId, String groupName, String groupAlias);

}
