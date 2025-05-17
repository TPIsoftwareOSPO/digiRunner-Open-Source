package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1004Req;
import tpi.dgrv4.dpaa.vo.AA1004Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpnApiModule;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.TsmpnApiModuleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1004Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpnApiModuleDao tsmpnApiModuleDao;
	private TsmpUserDao tsmpUserDao;
	
	@Autowired
	public AA1004Service(TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao,
			TsmpApiModuleDao tsmpApiModuleDao, TsmpnApiModuleDao tsmpnApiModuleDao, TsmpUserDao tsmpUserDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpnApiModuleDao = tsmpnApiModuleDao;
		this.tsmpUserDao = tsmpUserDao;
	}

	@Transactional
	public AA1004Resp deleteTOrgByOrgId (TsmpAuthorization auth, AA1004Req req) {
		AA1004Resp resp = new AA1004Resp();
		try {
			
			checkParams(req);
			
			updateTsmpOrganizationTable(auth, req);	
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();			//1287:刪除失敗
		}
		
		return resp;
	}
	
	/**
	 * 檢查邏輯
	 * 
	 * @param req
	 * @throws Exception
	 */
	protected void checkParams(AA1004Req req) throws Exception {
		
		checkTsmpOrganization(req);	//檢查 TSMP_ORGANIZATION資料表
		checkTsmpApi(req);			//檢查 TSMP_API資料表
		checkTsmpUser(req);			//檢查 TSMP_USER資料表
		checkTsmpAapModule(req);	//檢查 TSMP_API_MODULE資料表
		checktsmpnApiModule(req);	//檢查 TSMPN_API_MODULE資料表
		
		
	}
	
	/**
	 * 1229:組織名稱不存在
	 * 1308:組織包含未刪除的子組織
	 * 
	 * @param req
	 */
	private void checkTsmpOrganization(AA1004Req req) {
		String orgId = ServiceUtil.nvl(req.getOrgId());
		String orgPath = "";

		// 查詢TSMP_ORGANIZATION資料表，條件ORG_ID = AA1004Req.orgId，若沒查詢到資料則throw RTN CODE 1229:組織名稱不存在。
		Optional<TsmpOrganization> optOrg = getTsmpOrganizationDao().findById(orgId);
		if(!optOrg.isPresent()) 
				throw TsmpDpAaRtnCode._1229.throwing();

		orgPath = optOrg.get().getOrgPath();
		
		// 查詢TSMP_ORGANIZATION資料表，條件ORG_PATH LIKE ? 步驟2找到的ORG_PATH + % (100000::100001::100002::100004%)，找出是否有子節點，
		// 若有查到查詢到資料則throw RTN CODE 1308:組織包含未刪除的子組織。
		List<TsmpOrganization> orgList = getTsmpOrganizationDao().findByOrgPathStartsWith(orgPath + "::");
		if(orgList != null && orgList.size() > 0)
			throw TsmpDpAaRtnCode._1308.throwing();
		
	}
	
	/**
	 * 1304:組織包含未刪除的API
	 * 
	 * @param req
	 */
	private void checkTsmpApi(AA1004Req req) {
		// 查詢TSMP_API資料表，條件ORG_ID = AA1004Req.orgId，若有查詢到資料則throw RTN CODE 1304:組織包含未刪除的API。
		String orgId = ServiceUtil.nvl(req.getOrgId());
		
		List<TsmpApi> apiList = getTsmpApiDao().findByOrgId(orgId);
		
		if(apiList != null && apiList.size() > 0)
			throw TsmpDpAaRtnCode._1304.throwing();
		
	}
	
	/**
	 *1305:該組織包含未刪除的用戶
	 * 
	 * @param req
	 */
	private void checkTsmpUser(AA1004Req req) {
		// 查詢TSMP_USER資料表，條件ORG_ID = AA1004Req.orgId，若有查詢到資料則throw RTN CODE 1305:該組織包含未刪除的用戶
		String orgId = ServiceUtil.nvl(req.getOrgId());
		List<TsmpUser> tsmpUserList = getTsmpUserDao().findByOrgId(orgId);
	
		if(tsmpUserList != null && tsmpUserList.size() > 0)
			throw TsmpDpAaRtnCode._1305.throwing();
		
	}

	/**
	 * 1306:組織包含未刪除的Java模組
	 * 
	 * @param req
	 */
	private void checkTsmpAapModule(AA1004Req req) {
		// 查詢TSMP_API_MODULE資料表，條件ORG_ID = AA1004Req.orgId，若有查詢到資料則throw RTN CODE 1306:組織包含未刪除的Java模組
		String orgId = ServiceUtil.nvl(req.getOrgId());
		// 不可用這個寫法太花費時間了 (因為取出 BLOB)
		//List<TsmpApiModule> apiModuleList = getTsmpApiModuleDao().findByOrgId(orgId);
		List<TsmpApiModule> apiModuleList = getTsmpApiModuleDao().queryListByOrgId(orgId);
		
		if(apiModuleList != null && apiModuleList.size() > 0)
				throw TsmpDpAaRtnCode._1306.throwing();
		
	}
	
	/**
	 * 1307::組織包含未刪除的.Net模組
	 * 
	 * @param req
	 */
	private void checktsmpnApiModule(AA1004Req req) {
		// 查詢TSMPN_API_MODULE資料表，條件ORG_ID = AA1004Req.orgId，若有查詢到資料則throw RTN CODE 1307::組織包含未刪除的.Net模組
		String orgId = ServiceUtil.nvl(req.getOrgId());
		// 不可用這個寫法太花費時間了 (因為取出 BLOB)
		//List<TsmpnApiModule> nApiModuleList = getTsmpnApiModuleDao().findByOrgId(orgId);
		List<TsmpnApiModule> nApiModuleList = getTsmpnApiModuleDao().queryListByOrgId(orgId);
		
		if(nApiModuleList != null && nApiModuleList.size() > 0)
			throw TsmpDpAaRtnCode._1307.throwing();
			
	}
	
	/**
	 * delete TSMP_ORGANIZATION
	 * 
	 * @param auth
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private void updateTsmpOrganizationTable(TsmpAuthorization auth, AA1004Req req) throws Exception {
		String orgId = req.getOrgId();
		String orgName = req.getOrgName();
		List<TsmpOrganization> orgList = getTsmpOrganizationDao().findByOrgIdAndOrgName(orgId, orgName);
	
		if(orgList != null && orgList.size() == 1) {
			
			TsmpOrganization tsmpOrganization = orgList.get(0);
			getTsmpOrganizationDao().delete(tsmpOrganization);
			
		}else {
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		
	}
	
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpnApiModuleDao getTsmpnApiModuleDao() {
		return this.tsmpnApiModuleDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}


}
