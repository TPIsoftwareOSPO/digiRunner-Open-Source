package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpNodeTaskWork;

@Repository
public interface TsmpNodeTaskWorkDao extends JpaRepository<TsmpNodeTaskWork, Long> {
	
	public List<TsmpNodeTaskWork> queryTaskStatus(TsmpNodeTaskWork lastRecord, String[] words, Long nodeTaskId, 
			Boolean isSuccess, int pageSize);
	
}
