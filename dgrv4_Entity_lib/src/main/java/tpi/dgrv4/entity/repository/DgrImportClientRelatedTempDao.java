package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrImportClientRelatedTemp;

@Repository
public interface DgrImportClientRelatedTempDao extends JpaRepository<DgrImportClientRelatedTemp, Long>, DgrImportClientRelatedTempSuperDao {

	public List<DgrImportClientRelatedTemp> findByCreateUser(String createUser);
	
	public List<DgrImportClientRelatedTemp> findByCreateDateTimeBefore(Date createDateTime);
		
}
