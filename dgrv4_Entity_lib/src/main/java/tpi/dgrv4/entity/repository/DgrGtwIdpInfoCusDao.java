package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;

@Repository
public interface DgrGtwIdpInfoCusDao extends JpaRepository<DgrGtwIdpInfoCus, Long>, DgrGtwIdpInfoCusSuperDao {

	Optional<DgrGtwIdpInfoCus> findByGtwIdpInfoCusId(Long gtwIdpInfoCusId);

	Optional<DgrGtwIdpInfoCus> findByGtwIdpInfoCusIdAndClientId(Long id, String clientId);

	Optional<DgrGtwIdpInfoCus> findFirstByGtwIdpInfoCusIdAndClientIdAndStatusOrderByGtwIdpInfoCusIdDesc(
			Long gtwIdpInfoCusId, String clientId, String status);

	Optional<DgrGtwIdpInfoCus> findFirstByClientIdAndStatusOrderByGtwIdpInfoCusIdDesc(String clientId, String status);

	List<DgrGtwIdpInfoCus> findByGtwIdpInfoCusIdAndClientIdAndStatusOrderByUpdateDateTimeDescGtwIdpInfoCusIdDesc(
			Long gtwIdpInfoCusId, String clientId, String status, String[] keywords, Integer pageSize);
}
