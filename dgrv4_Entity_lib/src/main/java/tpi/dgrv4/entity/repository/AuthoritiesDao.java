package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.AuthoritiesId;

@Repository
public interface AuthoritiesDao extends JpaRepository<Authorities, AuthoritiesId> {

	public List<Authorities> findByUsername(String username);

	public List<Authorities> findByAuthority(String authority);

	public long deleteByUsername(String username);

	public Authorities findFirstByUsername(String userName);
}
