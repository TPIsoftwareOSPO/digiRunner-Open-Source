package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;

@Repository
public interface DgrAcIdpInfoMLdapDDao extends JpaRepository<DgrAcIdpInfoMLdapD, Long>, DgrAcIdpInfoMLdapDSuperDao {

	public List<DgrAcIdpInfoMLdapD> findByCreateUser(String createUser);

	public List<DgrAcIdpInfoMLdapD> findAllByRefAcIdpInfoMLdapMId(Long refAcIdpInfoMLdapMId);
	
	public List<DgrAcIdpInfoMLdapD> findAllByRefAcIdpInfoMLdapMIdOrderByOrderNoAsc(Long refAcIdpInfoMLdapMId);

	public List<DgrAcIdpInfoMLdapD> findAllByRefAcIdpInfoMLdapMIdOrderByOrderNoAscAcIdpInfoMLdapDIdAsc(
			Long refAcIdpInfoMLdapMId);
	
	public void deleteByRefAcIdpInfoMLdapMId(Long refAcIdpInfoMLdapMId);
}