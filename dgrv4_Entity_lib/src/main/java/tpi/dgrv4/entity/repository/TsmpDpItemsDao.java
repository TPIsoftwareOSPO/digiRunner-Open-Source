package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.ITsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;

@Repository
public interface TsmpDpItemsDao extends JpaRepository<TsmpDpItems, TsmpDpItemsId> {
	
	public List<ITsmpDpItems> queryBcryptParam(String itemNo, String locale);
	
	public List<TsmpDpItems> findByCreateUser(String createUser);
	
	public TsmpDpItems findByItemNoAndParam1AndLocale(String itemNo, String param1, String locale);
	
	public List<TsmpDpItems> findByLocale(String locale, Sort sort);
	
	public List<TsmpDpItems> findByLocale(String locale);
	
	public List<TsmpDpItems> findByCreateUser(String createUser, Sort sort);
		
	public List<TsmpDpItems> findByItemNoAndLocale(String itemNo, String locale);
	
	public List<TsmpDpItems> findByItemNoAndLocaleOrderBySortByAsc(String itemNo, String locale);

	public TsmpDpItems findByItemNoAndParam2AndLocale(String itemNo, String param2, String locale);
	
	public TsmpDpItems findByItemNoAndSubitemNoAndLocale(String itemNo, String subitemNo, String locale);
	
	public List<TsmpDpItems> findByItemNo(String itemNo, Sort sort);
		
	//DPB0047, DPF0029
	public List<TsmpDpItems> queryLike(Long lastId, String[] keyword, String itemNo, 
			String isDefault, int pageSize, String locale);

	public ITsmpDpItems saveAndFlush(ITsmpDpItems item);
	
	public List<TsmpDpItems> findAllByOrderByItemIdAsc();
	
	public List<TsmpDpItems> findByItemIdAndLocale(Long itemId, String locale);
	
}