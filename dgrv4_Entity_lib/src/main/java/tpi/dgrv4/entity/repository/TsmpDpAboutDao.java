package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.sql.TsmpDpAbout;

@Repository
public interface TsmpDpAboutDao extends JpaRepository<TsmpDpAbout, Long> {

	public List<TsmpDpAbout> findByCreateUser(String createUser);

}
