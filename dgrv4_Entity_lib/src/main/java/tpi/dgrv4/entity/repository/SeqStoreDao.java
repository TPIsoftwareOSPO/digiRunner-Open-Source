package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.SeqStore;

@Repository
@Transactional
public interface SeqStoreDao extends JpaRepository<SeqStore, String> {

	public Long nextSequence(String sequenceName, Long initial, Long increment);
	
	public List<SeqStore> queryExpiredSequence(String reqParam, String today_yyyyMMdd);

}
