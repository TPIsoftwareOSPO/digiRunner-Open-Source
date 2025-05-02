package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpnNodeTask;

@Repository
public interface TsmpnNodeTaskDao extends JpaRepository<TsmpnNodeTask, Long> {
	
	public List<TsmpnNodeTask> queryTaskList_1(TsmpnNodeTask lastRecord, String[] words, Date startDate, Date endDate,
			int pageSize);
}
