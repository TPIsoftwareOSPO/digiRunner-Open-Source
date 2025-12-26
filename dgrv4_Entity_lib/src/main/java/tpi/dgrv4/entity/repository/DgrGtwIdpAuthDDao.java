package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpAuthD;

@Repository
public interface DgrGtwIdpAuthDDao extends JpaRepository<DgrGtwIdpAuthD, Long> {
	
	public List<DgrGtwIdpAuthD> findByCreateUser(String createUser);
	
	public List<DgrGtwIdpAuthD> findByRefGtwIdpAuthMId(long refGtwIdpAuthMId);

}
