package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1001Req;
import tpi.dgrv4.dpaa.vo.AA1001Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpSequenceDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1001Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private SeqStoreService seqStoreService;
	private TsmpSequenceDao tsmpSequenceDao;
	private SeqStoreDao seqStoreDao;

	@Autowired
	public AA1001Service(TsmpOrganizationDao tsmpOrganizationDao, SeqStoreService seqStoreService,
			TsmpSequenceDao tsmpSequenceDao, SeqStoreDao seqStoreDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.seqStoreService = seqStoreService;
		this.tsmpSequenceDao = tsmpSequenceDao;
		this.seqStoreDao = seqStoreDao;
	}


	public AA1001Resp addTOrg(TsmpAuthorization auth, AA1001Req req) {
		AA1001Resp resp = new AA1001Resp();
		
		try {
			
			checkParams(req);
			
			String orgId = addTsmpOrganizationTable(auth, req);
			resp.setOrgId(orgId);
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();			//1288:新增失敗
		}
		
		return resp;
	}
	

	/**
	 * 檢查邏輯
	 * 
	 * @param req
	 * @throws Exception
	 */
	protected void checkParams(AA1001Req req) throws Exception {
		
		checkOrgName(req);			//檢查 組織名稱
		checkParentId(req);			//檢查 上層組織名稱
		checkContactTel(req);		//檢查 聯絡人電話
		checkContactName(req);		//檢查 聯絡人姓名
		checkContactEmail(req);		//檢查 聯絡人信箱
		checkOrgCode(req);			//檢查 組織代碼
		
	}
	
	/**
	 * 1250:組織名稱:必填參數 
	 * 1253:組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * 1269:組織名稱已存在
	 * 
	 * @param req
	 */
	private void checkOrgName(AA1001Req req) {
		String orgName = ServiceUtil.nvl(req.getOrgName());
		
		// 1250:組織名稱:必填參數 
		if(StringUtils.isEmpty(orgName))
			throw TsmpDpAaRtnCode._1250.throwing();
		
		// 1253:組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
		String msg = "";
		if(orgName.length() > 100) {
			int length = orgName.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1253.throwing("100", msg);
		}
		
		// 查詢TSMP_ORGANIZATION資料表，條件ORG_NAME = AA1001Req.orgName，若有查詢到資料則throw RTN CODE 1269:組織名稱已存在
		List<String> orgList = getTsmpOrganizationDao().findByOrgName(orgName, 1);
		if(orgList != null && orgList.size() > 0)
			throw TsmpDpAaRtnCode._1269.throwing();
	}

	/**
	 * 1271:上層組織名稱:必填參數 
	 * 1272:上層組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * @param req
	 */
	private void checkParentId(AA1001Req req) {
		String parentId = ServiceUtil.nvl(req.getParentId());

		// 1271:上層組織名稱:必填參數
		if(StringUtils.isEmpty(parentId))
				throw TsmpDpAaRtnCode._1271.throwing();
		
		// 1272:上層組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		String msg = "";
		if(parentId.length() > 30) {
			int length = parentId.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1272.throwing("30", msg);
		}
	}
	
	/**
	 * 1276:聯絡人電話:必填參數 
	 * 1277:聯絡人電話:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * 
	 * @param req
	 */
	private void checkContactTel(AA1001Req req) {
		String contactTel = ServiceUtil.nvl(req.getContactTel());
		
		// 1276:聯絡人電話:必填參數 
		if(StringUtils.isEmpty(contactTel))
				throw TsmpDpAaRtnCode._1276.throwing();
		
		// 1277:聯絡人電話:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
		String msg = "";
		if(contactTel.length() > 50) {
			int length = contactTel.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1277.throwing("50", msg);
		}
	}
	
	/**
	 * 1278:聯絡人姓名:必填參數 
	 * 1279:聯絡人姓名:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * 
	 * @param req
	 */
	private void checkContactName(AA1001Req req) {
		String contactName = ServiceUtil.nvl(req.getContactName());

		// 1278:聯絡人姓名:必填參數 
		if(StringUtils.isEmpty(contactName))
				throw TsmpDpAaRtnCode._1278.throwing();
		
		// 1279:聯絡人姓名:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
		String msg = "";
		if(contactName.length() > 30) {
			int length = contactName.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1279.throwing("50", msg);
		}
	}
	
	
	/**
	 * 1280:聯絡人信箱:必填參數 
	 * 1281:聯絡人信箱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * 1311:聯絡人信箱:只能為Email格式 
	 * 
	 * @param req
	 */
	private void checkContactEmail(AA1001Req req) {
		String contactEmail = ServiceUtil.nvl(req.getContactMail());

		// 1280:聯絡人信箱:必填參數 
		if(StringUtils.isEmpty(contactEmail))
				throw TsmpDpAaRtnCode._1280.throwing();
		
		// 1281:聯絡人信箱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
		String msg = "";
		if(contactEmail.length() > 100) {
			int length = contactEmail.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1281.throwing("100", msg);
		}
		
		// 1311:聯絡人信箱:只能為Email格式 
		if (!ServiceUtil.checkEmail(contactEmail))
			throw TsmpDpAaRtnCode._1311.throwing();
	}
	
	/**
	 * 組織代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param req
	 */
	private void checkOrgCode(AA1001Req req) {
		String orgCode = ServiceUtil.nvl(req.getOrgCode());
		
		// 1275:組織代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		String msg = "";
		if(orgCode.length() > 100) {
			int length = orgCode.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1275.throwing("100", msg);
		}
	}
	
	/**
	 * 新增TSMP_ORGANIZATION
	 * 
	 * @param auth
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private String addTsmpOrganizationTable(TsmpAuthorization auth, AA1001Req req) throws Exception {
		String orgId = "";
		
		orgId = getOrgId();
		
		TsmpOrganization tsmpOrganization = new TsmpOrganization();
		tsmpOrganization.setOrgId(orgId);
		tsmpOrganization.setContactMail(ServiceUtil.nvl(req.getContactMail()));
		tsmpOrganization.setContactName(ServiceUtil.nvl(req.getContactName()));
		tsmpOrganization.setContactTel(ServiceUtil.nvl(req.getContactTel()));
		tsmpOrganization.setCreateTime(DateTimeUtil.now());
		tsmpOrganization.setCreateUser(ServiceUtil.nvl(auth.getUserName()));
		tsmpOrganization.setOrgCode(ServiceUtil.nvl(req.getOrgCode()));
		tsmpOrganization.setOrgName(ServiceUtil.nvl(req.getOrgName()));
//		以AA1001Req.parentId為條件，找出完整節點路徑(包含本身節點)如右下角圖的TSMP_ORGANIZATION.ORG_PATHX，以::為區隔
		String parentId = ServiceUtil.nvl(req.getParentId());
		tsmpOrganization.setParentId(parentId);
		tsmpOrganization.setOrgPath(getOrgPath(parentId, orgId));
		
		tsmpOrganization.setUpdateTime(DateTimeUtil.now());
		tsmpOrganization.setUpdateUser(ServiceUtil.nvl(auth.getUserName()));
		
		
		getTsmpOrganizationDao().saveAndFlush(tsmpOrganization);
		
		return orgId;
		
	}
	
	protected String getOrgId() throws Exception {
		String orgId = "";
		
		Long roleIdLong = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_ORGANIZATION_PK);
		if (roleIdLong != null) {
			orgId = roleIdLong.toString();
		}
		
		if(StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return orgId;
	}
	
	private String getOrgPath(String parentId, String orgId) {
		String aa1001_orgPath = "";
		String aa1001_parentpath = "";
		Optional<TsmpOrganization> aa1001_optParentOrg =  getTsmpOrganizationDao().findById(parentId);
		if(aa1001_optParentOrg.isPresent()) {
			
			aa1001_parentpath = aa1001_optParentOrg.get().getOrgPath();
			if(!"".equals(aa1001_parentpath)) {
				aa1001_orgPath = aa1001_parentpath + "::" + orgId;
			}else {
				logger.debug("getOrgPath parentId = "+ parentId + " orgPath is empty !");
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
		}else {
			logger.debug("getOrgPath parentId = "+ parentId + "  is empty !");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return aa1001_orgPath;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

	protected TsmpSequenceDao getTsmpSequenceDao() {
		return this.tsmpSequenceDao;
	}
	
	protected SeqStoreDao getSeqStoreDao() {
		return this.seqStoreDao;
	}

}
