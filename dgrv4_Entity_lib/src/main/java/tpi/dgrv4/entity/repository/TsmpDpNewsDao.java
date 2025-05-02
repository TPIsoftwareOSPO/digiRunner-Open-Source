package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;

@Repository
public interface TsmpDpNewsDao extends JpaRepository<TsmpDpNews, Long> {
	
	//DPB0045, DPF0036
	public List<TsmpDpNews> queryId(List<String> orgDescList, Long newsId);
	
	//DPB0044, DPF0037
	public List<TsmpDpNews> queryLike(TsmpDpNews lastRecord, String[] words, Date startDate, Date endDate, Date nowDate, 
			String typeItemNo, String enFlag, String fbType, int pageSize);

	public List<TsmpDpNews> query_dpb0046Job(Date expDate);
	
	public List<TsmpDpNews> findByCreateUser(String createUser);
	
	public TsmpDpNews findByNewsIdAndStatus(Long newsId, String status);
 
}
