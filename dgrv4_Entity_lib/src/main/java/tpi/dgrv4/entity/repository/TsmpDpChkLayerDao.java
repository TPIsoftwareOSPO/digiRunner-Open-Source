package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayer;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayerId;

@Repository
public interface TsmpDpChkLayerDao extends JpaRepository<TsmpDpChkLayer, TsmpDpChkLayerId> {
	
	public List<TsmpDpChkLayer> queryByReviewTypeAndRoleIdList(String reviewType, List<String> roleIdList);
	
	public List<Integer> queryForCreateReqOrders(String reviewType);

	public Boolean isUserAuthorized(String reviewType, Integer layer, String username);

	public List<TsmpDpChkLayer> findByCreateUser(String createUser);
	
	public List<TsmpDpChkLayer> findByStatus(String status);

	public List<TsmpDpChkLayer> findByReviewTypeAndStatus(String reviewType, String status);

}
