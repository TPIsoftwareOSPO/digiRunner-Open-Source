package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;

@Repository
public interface TsmpDpApptRjobDDao extends JpaRepository<TsmpDpApptRjobD, Long> {
	
	public List<TsmpDpApptRjobD> findByApptRjobIdOrderBySortByAscApptRjobDIdAsc(String apptRjobId);

	/** 依週期排程UID及週期工作ID找出下一個要執行的工作項目, 如果 apptRjobDId = null, 則找出排序第一的項目 */
	public TsmpDpApptRjobD queryNextRjobD(String apptRjobId, Long apptRjobDId);

	public List<TsmpDpApptRjobD> findByCreateUser(String createUser);

}