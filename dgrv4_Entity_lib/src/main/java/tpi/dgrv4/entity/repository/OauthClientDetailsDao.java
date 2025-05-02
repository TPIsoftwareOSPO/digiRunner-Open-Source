package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.OauthClientDetails;

@Repository
public interface OauthClientDetailsDao extends JpaRepository<OauthClientDetails, String> {

	public void deleteByClientId(String clientId);
}