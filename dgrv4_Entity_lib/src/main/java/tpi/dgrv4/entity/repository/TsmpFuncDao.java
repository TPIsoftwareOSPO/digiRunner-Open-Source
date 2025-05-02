package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpFuncId;

@Repository
public interface TsmpFuncDao extends JpaRepository<TsmpFunc, TsmpFuncId> {

	public List<TsmpFunc> findByLocaleOrderByFuncCodeAsc(String locale);

	public List<TsmpFunc> findByFuncCode(String funcCode);

	public List<TsmpFunc> query_aa0103Service(String funcCode, String locale, String[] words, Integer pageSize, String funcType);
	
	public List<String> queryAllFuncCode();

	public List<TsmpFunc> findByFuncCodeStartsWith(String funcCodePrefix);

	public List<TsmpFunc> findByLocale(String locale);
	
	public List<TsmpFunc> findAllByFuncType(String funcType);
	
	public List<TsmpFunc> findMasterFuncList(String locale);
	
	public List<TsmpFunc> findByLocaleAndFuncTypeAndFuncCodeStartsWithOrderByFuncCodeAsc(String locale, String funcType, String funcCode);
}
