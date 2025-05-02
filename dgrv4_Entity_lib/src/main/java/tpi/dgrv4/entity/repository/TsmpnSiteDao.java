package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.sql.TsmpnSite;

@Repository
public interface TsmpnSiteDao extends JpaRepository<TsmpnSite, Long> {

	public List<TsmpnSite> findByCreateUser(String createUser);

}
