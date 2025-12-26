package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.ITsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.ITsmpRtnCodeDao;

@Repository
public interface TsmpRtnCodeDao extends JpaRepository<TsmpRtnCode, TsmpRtnCodeId>, ITsmpRtnCodeDao {

	public Optional<ITsmpRtnCode> findByTsmpRtnCodeAndLocale(String tsmpRtnCode, String locale);

	public List<TsmpRtnCode> query_dpb0097service_01(String rtnCode, String locale, String[] words,  int pageSize);
	
	public List<TsmpRtnCode> findAllByOrderByTsmpRtnCodeAsc();

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("update TsmpRtnCode RC set RC.tsmpRtnMsg = :newMsg , RC.tsmpRtnDesc = :newDesc where 1 = 1 and RC.tsmpRtnCode = :rtnCode and RC.locale = :locale and RC.tsmpRtnMsg = :oldMsg")
	
	public int update_dpb0099service_01(
			@Param("newMsg") String newMsg,
			@Param("newDesc") String newDesc,
			@Param("rtnCode") String tsmpRtnCode,
			@Param("locale") String locale,
			@Param("oldMsg") String oldMsg
			);
	
}