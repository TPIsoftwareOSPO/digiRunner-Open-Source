package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpNodeTask;

@Repository
public interface TsmpNodeTaskDao extends JpaRepository<TsmpNodeTask, Long> {
	
	public List<TsmpNodeTask> queryTaskList_1(TsmpNodeTask lastRecord, String[] words, Date startDate, Date endDate,
			int pageSize);

	public TsmpNodeTask findFirstByTaskSignatureAndTaskId(String taskSignature, String taskId);

}