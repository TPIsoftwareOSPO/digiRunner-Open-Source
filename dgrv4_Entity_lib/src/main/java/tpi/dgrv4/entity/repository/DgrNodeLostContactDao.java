package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.DgrNodeLostContact;

@Repository
public interface DgrNodeLostContactDao extends JpaRepository<DgrNodeLostContact, Long> {
	
	public List<DgrNodeLostContact> findByCreateUser(String createUser);
	
	@Modifying
	@Transactional
	public Long deleteByCreateTimestampLessThan(long createTimestamp);
	public List<DgrNodeLostContact> findByCreateTimestampGreaterThanEqual(long createTimestamp, Sort sort);
}