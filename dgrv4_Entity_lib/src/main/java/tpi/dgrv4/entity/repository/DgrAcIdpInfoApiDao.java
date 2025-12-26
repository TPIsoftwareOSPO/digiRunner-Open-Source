package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
@Repository
public interface DgrAcIdpInfoApiDao extends JpaRepository<DgrAcIdpInfoApi, Long>, DgrAcIdpInfoApiSuperDao {

	List<DgrAcIdpInfoApi> findByCreateUser(String testUser);

	DgrAcIdpInfoApi findFirstByStatusOrderByCreateDateTimeDesc(String Status ) ;
	
	List<DgrAcIdpInfoApi>  findAllByOrderByCreateDateTimeDescAcIdpInfoApiIdDesc();
	
}
