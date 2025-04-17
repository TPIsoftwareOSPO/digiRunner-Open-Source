package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1003Req;
import tpi.dgrv4.dpaa.vo.AA1003Resp;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1003Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public AA1003Service(TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	@Transactional
	public AA1003Resp updateTOrgByOrgId(TsmpAuthorization auth, AA1003Req req) {
		AA1003Resp resp = new AA1003Resp();
		try {
			
			checkParams(req);
			
			Map<String, String> map = updateTsmpOrganizationTable(auth, req);	
			if(map == null || map.size() ==0)
				throw TsmpDpAaRtnCode._1297.throwing();		// 1297:執行錯誤
				
			String orgId = map.get("orgId");
			String updateTime = map.get("updateTime");
			
			resp.setOrgId(orgId);
			resp.setUpdateTime(updateTime);
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();			//1286:更新失敗
		}
		
		return resp;
	}
	
	/**
	 * 檢查邏輯
	 * 
	 * @param req
	 * @throws Exception
	 */
	protected void checkParams(AA1003Req req) throws Exception {
		
		
		checkOrgId(req);				//檢查 組織單位ID
		checkNewOrgName(req);			//檢查 組織名稱
		
		if(!"100000".equals(req.getOrgId())) {
			checkNewParentId(req);			//檢查 上層組織名稱
			checkNewOrgCode(req);			//檢查 組織代碼
			checkNewContactTel(req);		//檢查 聯絡人電話
			checkNewContactName(req);		//檢查 聯絡人姓名
			checkNewContactEmail(req);		//檢查 聯絡人信箱
			
			checkNode(req);					//節點不可以移動到子節點或自己
		}
	}
	
	/**
	 * 1271:上層組織名稱:必填參數
	 * 1272:上層組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param req
	 */
	private void checkNewParentId(AA1003Req req) {
		String parentId = ServiceUtil.nvl(req.getNewParentId());

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
	 * 1273:組織單位ID:必填參數
	 * 1274:組織單位ID:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param req
	 */
	private void checkOrgId(AA1003Req req) {
		String orgId = ServiceUtil.nvl(req.getOrgId());
		
		// 1273:組織單位ID:必填參數
		if(StringUtils.isEmpty(orgId))
			throw TsmpDpAaRtnCode._1273.throwing();
		
		// 1274:組織單位ID:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		String msg = "";
		if(orgId.length() > 30) {
			int length = orgId.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1274.throwing("30", msg);
		}
		
		// 查詢TSMP_ORGANIZATION資料表，條件ORG_ID = AA1003Req.orgId，若沒查詢到資料則throw RTN CODE 1229:組織名稱不存在
		Optional<TsmpOrganization> optOrg = getTsmpOrganizationDao().findById(orgId);
		if(!optOrg.isPresent())
			throw TsmpDpAaRtnCode._1229.throwing();
	}
	
	/**
	 * 1250:組織名稱:必填參數 
	 * 1253:組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * 1269:組織名稱已存在
	 * 
	 * @param req
	 */
	private void checkNewOrgName(AA1003Req req) {
		String newOrgName = ServiceUtil.nvl(req.getNewOrgName());
		String orgName = ServiceUtil.nvl(req.getOrgName());
		// 1250:組織名稱:必填參數 
		if(StringUtils.isEmpty(newOrgName))
			throw TsmpDpAaRtnCode._1250.throwing();
		
		// 1253:組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
		String msg = "";
		if(newOrgName.length() > 100) {
			int length = newOrgName.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1253.throwing("100", msg);
		}
		
		// 查詢TSMP_ORGANIZATION資料表，條件ORG_NAME = AA1001Req.orgName，若有查詢到資料則throw RTN CODE 1269:組織名稱已存在
		if(!orgName.equals(newOrgName)) {
			List<String> orgList = getTsmpOrganizationDao().findByOrgName(newOrgName, 1);
			if(orgList != null && orgList.size() > 0)
				throw TsmpDpAaRtnCode._1269.throwing();
		}
	}

	/**
	 * 1276:聯絡人電話:必填參數 
	 * 1277:聯絡人電話:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字 
	 * 
	 * @param req
	 */
	private void checkNewContactTel(AA1003Req req) {
		String contactTel = ServiceUtil.nvl(req.getNewContactTel());
		
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
	private void checkNewContactName(AA1003Req req) {
		String contactName = ServiceUtil.nvl(req.getNewContactName());

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
	private void checkNewContactEmail(AA1003Req req) {
		String contactEmail = ServiceUtil.nvl(req.getNewContactMail());

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
	private void checkNewOrgCode(AA1003Req req) {
		String orgCode = ServiceUtil.nvl(req.getNewOrgCode());
		
		// 1275:組織代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		String msg = "";
		if(orgCode.length() > 100) {
			int length = orgCode.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1275.throwing("100", msg);
		}
	}
	
	/**
	 * 1312:上層單位組織：不可以選擇節點自己本身
	 * 1303:節點不可以移動到子節點
	 * 
	 * @param req
	 */
	private void checkNode(AA1003Req req) {
		String parentId = ServiceUtil.nvl(req.getParentId());
		String newParentId = ServiceUtil.nvl(req.getNewParentId());
		String orgId = ServiceUtil.nvl(req.getOrgId());

		// 檢查AA1003Req.newParentId是不是節點自己本身(AA1003Req.orgId)，若是則throw RTN CODE 1312:上層單位組織：不可以選擇節點自己本身
		if(orgId.equals(newParentId))
			throw TsmpDpAaRtnCode._1312.throwing();
		
		// 若parentId與newParentId不相同， 則表示節點異動需要更新本身節點與子節點的
		// UPDATE_USER、UPDATE_TIME、TSMP_ORGANIZATION.PARENT_ID與TSMP_ORGANIZATION.ORG_PATH(找到該組織及其下的所有子節點, 並更新其父orgid)。
		// 注意若節點移動到子節點，則throw RTN CODE 1303。
		
		// 檢查新的節點是不是移到自己底下的節點
		if(!parentId.equals(newParentId)) {
			Optional<TsmpOrganization> optOrg = getTsmpOrganizationDao().findById(parentId);
			Optional<TsmpOrganization> optNewOrg = getTsmpOrganizationDao().findById(newParentId);
			if(optOrg.isPresent() && optNewOrg.isPresent()) {
				String orgPath = optOrg.get().getOrgPath()+"::"+orgId;
				String orgNewPath = optNewOrg.get().getOrgPath()+"::"+orgId;
				
				if(orgNewPath.startsWith(orgPath)) {
					throw TsmpDpAaRtnCode._1303.throwing();
				}
			}
		}
	}
	
	/**
	 * 更新TSMP_ORGANIZATION
	 * 
	 * @param auth
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private Map<String,String> updateTsmpOrganizationTable(TsmpAuthorization auth, AA1003Req req) throws Exception {
//		UPDATE_USER、UPDATE_TIME、TSMP_ORGANIZATION.PARENT_ID與TSMP_ORGANIZATION.ORG_PATH(找到該組織及其下的所有子節點, 並更新其父orgid)
		Map<String, String> map = new HashMap<>();
		String orgId = req.getOrgId();
		String originalOrgPath = "";
		Optional<TsmpOrganization> optOrg = getTsmpOrganizationDao().findById(orgId);
		if(optOrg.isPresent()) {
			
			TsmpOrganization tsmpOrganization = optOrg.get();
			Date updateTime =  DateTimeUtil.now();
			
			if(!"100000".equals(orgId)) {
				originalOrgPath = tsmpOrganization.getOrgPath();
				
				tsmpOrganization.setContactMail(ServiceUtil.nvl(req.getNewContactMail()));
				tsmpOrganization.setContactName(ServiceUtil.nvl(req.getNewContactName()));
				tsmpOrganization.setContactTel(ServiceUtil.nvl(req.getNewContactTel()));
				tsmpOrganization.setOrgCode(ServiceUtil.nvl(req.getNewOrgCode()));
				tsmpOrganization.setOrgName(ServiceUtil.nvl(req.getNewOrgName()));
				
				String newParentId = ServiceUtil.nvl(req.getNewParentId());
				tsmpOrganization.setParentId(newParentId);
				
				String orgPath = getOrgPath(newParentId, orgId);
				tsmpOrganization.setOrgPath(orgPath);
				tsmpOrganization.setUpdateTime(updateTime);
				tsmpOrganization.setUpdateUser(ServiceUtil.nvl(auth.getUserName()));
				
				getTsmpOrganizationDao().saveAndFlush(tsmpOrganization);
				
				// 也要更新子節點的PARENT_ID 和 ORG_PATH
				setChildParentIDAndOrgPath(auth, req, orgPath, originalOrgPath);
			}else {
				tsmpOrganization.setOrgName(ServiceUtil.nvl(req.getNewOrgName()));
				tsmpOrganization.setUpdateTime(updateTime);
				tsmpOrganization.setUpdateUser(ServiceUtil.nvl(auth.getUserName()));
				
				getTsmpOrganizationDao().saveAndFlush(tsmpOrganization);
			}
			
			map.put("orgId", orgId);
			map.put("updateTime", updateTime.toString());
		}
		return map;
	}
	
	private String getOrgPath(String parentId, String orgId) {
		String orgPath = "";
		String parentpath = "";
		Optional<TsmpOrganization> optParentOrg =  getTsmpOrganizationDao().findById(parentId);
		if(optParentOrg.isPresent()) {
			
			parentpath = optParentOrg.get().getOrgPath();
			if(!"".equals(parentpath)) {
				orgPath = parentpath + "::" + orgId;
			}else {
				logger.debug("getOrgPath parentId = "+ parentId + " orgPath is empty !");
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
		}else {
			logger.debug("getOrgPath parentId = "+ parentId + "  is empty !");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return orgPath;
	}
	
	
	private void setChildParentIDAndOrgPath(TsmpAuthorization auth, AA1003Req req, String newParentOrgPath, String originaParentOrgPath) {
		// 1.UPDATE_USER、2.UPDATE_TIME、3.TSMP_ORGANIZATION.PARENT_ID、4.TSMP_ORGANIZATION.ORG_PATH(找到該組織及其下的所有子節點, 並更新其父orgid)
		// 找出包含此 orgId 及其向下的所有組織 
		String orgId = req.getOrgId();
		
		List<TsmpOrganization> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_entity(orgId, Integer.MAX_VALUE);	
		orgList.forEach((org) -> {
			String childOrgId = org.getOrgId();
			if(!orgId.equals(childOrgId)) {
				// 子節點的parentId 不會異動 但是org_path會異動
				String bb = newParentOrgPath.substring(0, newParentOrgPath.indexOf(orgId)+orgId.length());
				String newChildOrgPath = bb+"::"+childOrgId;
				
				org.setOrgPath(newChildOrgPath);
				org.setUpdateUser(auth.getUserName());
				org.setUpdateTime(DateTimeUtil.now());
				
				getTsmpOrganizationDao().saveAndFlush(org);
				
			}
		});
		
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

}
