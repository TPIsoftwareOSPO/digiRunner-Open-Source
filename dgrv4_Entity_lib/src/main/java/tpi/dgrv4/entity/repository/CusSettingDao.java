package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.CusSetting;
import tpi.dgrv4.entity.entity.jpql.CusSettingId;

@Repository
public interface CusSettingDao extends JpaRepository<CusSetting, CusSettingId> {
	
	public List<CusSetting> findByCreateUser(String createUser);
	
	public List<CusSetting> queryDPB9910Service(Integer lastSortBy, String[] keywords, Integer pageSize);

	public CusSetting findFirstBySortBy(Integer sortBy);
	
	public List<CusSetting> findBySortBy(Integer sortBy);
}
