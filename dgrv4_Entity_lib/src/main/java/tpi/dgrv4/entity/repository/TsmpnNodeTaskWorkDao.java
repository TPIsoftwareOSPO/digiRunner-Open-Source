package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpnNodeTaskWork;

@Repository
public interface TsmpnNodeTaskWorkDao extends JpaRepository<TsmpnNodeTaskWork, Long> {
	
		public List<TsmpnNodeTaskWork> queryTaskStatus(TsmpnNodeTaskWork lastRecord, String[] words, Long nodeTaskId,
			Boolean isSuccess, int pageSize);
	
}
