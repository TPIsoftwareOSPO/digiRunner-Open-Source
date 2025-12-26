package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.entity.TsmpGroup;

@Repository
public interface DgrRdbConnectionDao extends JpaRepository<DgrRdbConnection, String> {
	
	public List<DgrRdbConnection> findByCreateUser(String createUser);
	
}
