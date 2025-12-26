package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;

@Repository
public interface TsmpRoleTxidMapDao extends JpaRepository<TsmpRoleTxidMap, Long> {

	public List<TsmpRoleTxidMap> findByRoleIdIn(List<String> roleIdList);
	
	public List<TsmpRoleTxidMap> findByRoleId(String roleId);

	public List<TsmpRoleTxidMap> findByTxid(String txid);

	public List<TsmpRoleTxidMap> findByListType(String listType);

	public List<TsmpRoleTxidMap> findByRoleIdAndTxid(String roleId, String txid);

	public List<TsmpRoleTxidMap> findByRoleIdAndListType(String roleId, String listType);

	public List<TsmpRoleTxidMap> query_dpb0111Service_01(String p_roleId, String p_listType, //
		String listType, String[] keywords);

	public List<TsmpRoleTxidMap> findByCreateUser(String createUser);

}