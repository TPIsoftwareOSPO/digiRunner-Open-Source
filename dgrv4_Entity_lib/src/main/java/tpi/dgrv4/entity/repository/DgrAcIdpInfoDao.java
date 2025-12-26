package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfo;

@Repository
public interface DgrAcIdpInfoDao extends JpaRepository<DgrAcIdpInfo, Long>, DgrAcIdpInfoSuperDao {

	public List<DgrAcIdpInfo> findByCreateUser(String createUser);

	public DgrAcIdpInfo findFirstByIdpTypeAndClientStatusOrderByCreateDateTimeDesc(String idpType, String clientStatus);

	public List<DgrAcIdpInfo> findByIdpTypeAndClientName(String idpType, String clientName);
	
	// idp_type + client_id 欄位為 UNIQUE
    public DgrAcIdpInfo findFirstByIdpTypeAndClientId(String idpType, String clientId);
    
	public List<DgrAcIdpInfo> findAllByOrderByCreateDateTimeDescAcIdpInfoIdDesc();
}
