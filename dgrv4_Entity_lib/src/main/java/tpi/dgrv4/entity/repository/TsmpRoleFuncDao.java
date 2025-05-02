package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.entity.TsmpRoleFuncId;

@Repository
public interface TsmpRoleFuncDao extends JpaRepository<TsmpRoleFunc, TsmpRoleFuncId> {

	public List<TsmpRoleFunc> findByFuncCode(String funcCode);
	
	public List<TsmpRoleFunc> findByFuncCodeStartsWith(String funcCode);

	public List<TsmpRoleFunc> findByRoleId(String roleId);

	public List<TsmpRoleFunc> findByRoleId(String roleId, Sort sort);

	public List<TsmpRoleFunc> queryByUserName(String userName);

}
