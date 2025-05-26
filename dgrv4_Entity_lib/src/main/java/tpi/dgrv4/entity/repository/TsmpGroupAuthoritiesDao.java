package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;

@Repository
public interface TsmpGroupAuthoritiesDao extends JpaRepository<TsmpGroupAuthorities, String> {

	public List<TsmpGroupAuthorities> queryByIdAndLevelAndKeyword(String lastGroupAuthoritieId, String[] keyword, List<String> selectedGroupAuthoritieIdList, int pageSize);
	
	public TsmpGroupAuthorities findFirstByGroupAuthoritieName(String groupAuthoritieName);
	
	public TsmpGroupAuthorities findFirstByGroupAuthoritieIdAndGroupAuthoritieName(String groupAuthoritieId, String groupAuthoritieName);
	
	public boolean existsByGroupAuthoritieId(String groupAuthoritieId);
	
	public boolean existsByGroupAuthoritieNameAndGroupAuthoritieIdNot(String groupAuthoritieName, String groupAuthoritieId);
	
	public boolean existsByGroupAuthoritieName(String groupAuthoritieName);
}
