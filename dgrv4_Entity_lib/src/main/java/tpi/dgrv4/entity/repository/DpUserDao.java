package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DpUser;

@Repository
public interface DpUserDao extends JpaRepository<DpUser, Long>, DpUserSuperDao {

	List<DpUser> findByUserIdentity(String userIdentity);
	
	Optional<DpUser> findByDpUserName(String dpUserName);

	Optional<DpUser> findByDpUserNameAndIss(String dpUserName, String iss);

	public List<DpUser> findByKeyword(Long userId, String userFlag, String[] keyword, int pageSize);
}
