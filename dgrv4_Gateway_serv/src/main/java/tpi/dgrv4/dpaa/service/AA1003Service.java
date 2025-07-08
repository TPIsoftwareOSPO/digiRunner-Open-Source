package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
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
	
	// 定義常數來解決重複字串問題
	private static final String MESSAGE_KEY = "message";
	
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
				
				// 【修正】在更新父節點之前，先取得所有需要更新的子孫節點清單
				List<TsmpOrganization> childOrgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_entity(orgId, Integer.MAX_VALUE);
				
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
				
				// 【修正】使用預先取得的子孫節點清單來更新
				setChildParentIDAndOrgPath(auth, req, orgPath, originalOrgPath, childOrgList);
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
				logger.debug(String.format("getOrgPath - ParentId %s (%s) has empty orgPath", 
						parentId, optParentOrg.get().getOrgName()));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
		}else {
			logger.debug(String.format("getOrgPath - ParentId %s not found", parentId));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return orgPath;
	}
	
	
	private void setChildParentIDAndOrgPath(TsmpAuthorization auth, AA1003Req req, String newParentOrgPath, String originaParentOrgPath, List<TsmpOrganization> childOrgList) {
		// 1.UPDATE_USER、2.UPDATE_TIME、3.TSMP_ORGANIZATION.PARENT_ID、4.TSMP_ORGANIZATION.ORG_PATH(找到該組織及其下的所有子節點, 並更新其父orgid)
		// 找出包含此 orgId 及其向下的所有組織 
		String orgId = req.getOrgId();
		
		childOrgList.forEach((org) -> {

			String childOrgId = org.getOrgId();
			if(!orgId.equals(childOrgId)) {
				// 【修正】使用相對路徑計算正確的新路徑
				String currentOrgPath = org.getOrgPath();
				
				// 計算子節點相對於移動節點的相對路徑
				// 例如：originalParentOrgPath = "100000::2000000041::2000000042::2000000043"
				//      currentOrgPath = "100000::2000000041::2000000042::2000000043::2000000044::2000000045"
				//      relativePath = "::2000000044::2000000045"
				String relativePath = currentOrgPath.substring(originaParentOrgPath.length());
				
				// 新路徑 = 移動節點的新路徑 + 相對路徑
				// 例如：newParentOrgPath = "100000::2000000043"
				//      newChildOrgPath = "100000::2000000043::2000000044::2000000045"
				String newChildOrgPath = newParentOrgPath + relativePath;
				
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
	
	/**
	 * 檢測並修復所有組織節點的路徑錯誤
	 * 
	 * @param userName 執行修復的使用者名稱
	 * @return 修復結果統計
	 */
	public Map<String, Object> repairAllOrgPaths(String userName) {
		Map<String, Object> result = new HashMap<>();
		int totalCount = 0;
		int errorCount = 0;
		int fixedCount = 0;
		int skippedCount = 0;
		
		try {
			// 1. 取得所有組織節點
			List<TsmpOrganization> allOrgs = getTsmpOrganizationDao().findAll();
			totalCount = allOrgs.size();
			
			// 2. 建立 orgId -> TsmpOrganization 的對應表
			Map<String, TsmpOrganization> orgMap = new HashMap<>();
			for (TsmpOrganization org : allOrgs) {
				orgMap.put(org.getOrgId(), org);
			}
			
			// 3. 檢查每個節點的路徑是否正確
			List<TsmpOrganization> errorOrgs = new ArrayList<>();
			for (TsmpOrganization org : allOrgs) {
				// 先檢查是否有循環引用
				List<String> circularPath = new ArrayList<>();
				if (hasCircularReference(org.getOrgId(), orgMap, new HashSet<>(), circularPath)) {
					skippedCount++;
					String pathDescription = buildCircularPathDescription(circularPath, orgMap);
					TPILogger.tl.warn(String.format("Skipping repair for circular reference - OrgId: %s (%s), Path: %s", 
							org.getOrgId(), org.getOrgName(), pathDescription));
					continue;
				}
				
				String correctPath = calculateCorrectOrgPath(org.getOrgId(), orgMap);
				if (correctPath != null && !correctPath.equals(org.getOrgPath())) {
					errorOrgs.add(org);
					errorCount++;
					String currentPathFormatted = formatOrgPathWithNames(org.getOrgPath(), orgMap);
					String correctPathFormatted = formatOrgPathWithNames(correctPath, orgMap);
					TPILogger.tl.warn(String.format("Path error found - OrgId: %s (%s), Current: %s, Correct: %s", 
							org.getOrgId(), org.getOrgName(), currentPathFormatted, correctPathFormatted));
				}
			}
			
			// 4. 修復錯誤的路徑
			Date updateTime = DateTimeUtil.now();
			for (TsmpOrganization org : errorOrgs) {
				String correctPath = calculateCorrectOrgPath(org.getOrgId(), orgMap);
				org.setOrgPath(correctPath);
				org.setUpdateTime(updateTime);
				org.setUpdateUser(userName);
				getTsmpOrganizationDao().saveAndFlush(org);
				fixedCount++;
				String correctPathFormatted = formatOrgPathWithNames(correctPath, orgMap);
				TPILogger.tl.info(String.format("Path repaired - OrgId: %s (%s), New path: %s", 
						org.getOrgId(), org.getOrgName(), correctPathFormatted));
			}
			
			result.put("totalCount", totalCount);
			result.put("errorCount", errorCount);
			result.put("fixedCount", fixedCount);
			result.put("skippedCount", skippedCount);
			result.put("success", true);
			result.put(MESSAGE_KEY, String.format("Checked %d nodes, found %d errors, successfully repaired %d nodes, skipped %d circular reference nodes", 
					totalCount, errorCount, fixedCount, skippedCount));
			
		} catch (Exception e) {
			TPILogger.tl.error(String.format("Organization path repair failed: %s", StackTraceUtil.logStackTrace(e)));
			result.put("success", false);
			result.put(MESSAGE_KEY, "Repair process error: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 計算組織節點的正確路徑
	 * 
	 * @param orgId 組織ID
	 * @param orgMap 所有組織節點的對應表
	 * @return 正確的組織路徑
	 */
	private String calculateCorrectOrgPath(String orgId, Map<String, TsmpOrganization> orgMap) {
		return calculateCorrectOrgPath(orgId, orgMap, new HashSet<>());
	}
	
	/**
	 * 計算組織節點的正確路徑（內部方法，帶循環引用檢測）
	 * 
	 * @param orgId 組織ID
	 * @param orgMap 所有組織節點的對應表
	 * @param visited 已訪問的節點集合，用於檢測循環引用
	 * @return 正確的組織路徑，如果有循環引用則返回null
	 */
	private String calculateCorrectOrgPath(String orgId, Map<String, TsmpOrganization> orgMap, Set<String> visited) {
		TsmpOrganization org = orgMap.get(orgId);
		if (org == null) {
			return null;
		}
		
		// 檢測循環引用
		if (visited.contains(orgId)) {
			TPILogger.tl.warn(String.format("Circular reference detected for OrgId: %s (%s)", 
					orgId, org.getOrgName()));
			return null; // 發現循環引用，返回null
		}
		
		// 如果是根節點（沒有父節點或父節點為空）
		String parentId = org.getParentId();
		if (parentId == null || parentId.trim().isEmpty()) {
			return orgId;
		}
		
		// 將當前節點加入已訪問集合
		visited.add(orgId);
		
		// 遞迴計算父節點路徑
		String parentPath = calculateCorrectOrgPath(parentId, orgMap, visited);
		
		// 從已訪問集合中移除當前節點（回溯）
		visited.remove(orgId);
		
		if (parentPath == null) {
			TPILogger.tl.warn(String.format("Parent node not found or circular reference - OrgId: %s (%s), ParentId: %s", 
					orgId, org.getOrgName(), parentId));
			return orgId; // 如果找不到父節點或有循環引用，將此節點設為根節點
		}
		
		return parentPath + "::" + orgId;
	}
	
	/**
	 * 驗證組織樹狀結構的一致性
	 * 
	 * @return 驗證結果
	 */
	public Map<String, Object> validateOrgTreeConsistency() {
		Map<String, Object> result = new HashMap<>();
		List<String> errors = new ArrayList<>();
		
		try {
			List<TsmpOrganization> allOrgs = getTsmpOrganizationDao().findAll();
			Map<String, TsmpOrganization> orgMap = new HashMap<>();
			
			for (TsmpOrganization org : allOrgs) {
				orgMap.put(org.getOrgId(), org);
			}
			
			for (TsmpOrganization org : allOrgs) {
				validateOrgNode(org, orgMap, errors);
			}
			
			result.put("isValid", errors.isEmpty());
			result.put("errorCount", errors.size());
			result.put("errors", errors);
			
		} catch (Exception e) {
			TPILogger.tl.error(String.format("Organization tree validation failed: %s", StackTraceUtil.logStackTrace(e)));
			result.put("isValid", false);
			result.put(MESSAGE_KEY, "Validation process error: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 驗證單一組織節點
	 * 
	 * @param org 組織節點
	 * @param orgMap 所有組織節點的對應表
	 * @param errors 錯誤列表
	 */
	private void validateOrgNode(TsmpOrganization org, Map<String, TsmpOrganization> orgMap, List<String> errors) {
		// 檢查1: 父節點是否存在
		validateParentNodeExists(org, orgMap, errors);
		
		// 檢查2: 路徑是否與父子關係一致
		validateOrgPath(org, orgMap, errors);
		
		// 檢查3: 是否有循環引用
		validateCircularReference(org, orgMap, errors);
	}
	
	/**
	 * 驗證父節點是否存在
	 */
	private void validateParentNodeExists(TsmpOrganization org, Map<String, TsmpOrganization> orgMap, List<String> errors) {
		String parentId = org.getParentId();
		if (parentId != null && !parentId.trim().isEmpty() && !orgMap.containsKey(parentId)) {
			errors.add(String.format("Node %s (%s) has non-existent parent node %s", 
					org.getOrgId(), org.getOrgName(), parentId));
		}
	}
	
	/**
	 * 驗證組織路徑是否正確
	 */
	private void validateOrgPath(TsmpOrganization org, Map<String, TsmpOrganization> orgMap, List<String> errors) {
		// 先檢查是否有循環引用，如果有則跳過路徑驗證
		List<String> circularPath = new ArrayList<>();
		if (hasCircularReference(org.getOrgId(), orgMap, new HashSet<>(), circularPath)) {
			// 循環引用的錯誤會在 validateCircularReference 中處理，這裡跳過路徑驗證
			return;
		}
		
		String correctPath = calculateCorrectOrgPath(org.getOrgId(), orgMap);
		if (correctPath != null && !correctPath.equals(org.getOrgPath())) {
			String parentId = org.getParentId();
			
			// 格式化路徑以顯示節點名稱
			String currentPathFormatted = formatOrgPathWithNames(org.getOrgPath(), orgMap);
			String correctPathFormatted = formatOrgPathWithNames(correctPath, orgMap);
			
			// 取得父節點名稱用於更詳細的錯誤訊息
			String parentInfo = getParentInfo(parentId, orgMap);
			
			errors.add(String.format("Node %s (%s) has incorrect path - Current: %s, Should be: %s%s", 
					org.getOrgId(), org.getOrgName(), currentPathFormatted, correctPathFormatted, parentInfo));
		}
	}
	
	/**
	 * 取得父節點資訊
	 */
	private String getParentInfo(String parentId, Map<String, TsmpOrganization> orgMap) {
		if (parentId != null && !parentId.trim().isEmpty() && orgMap.containsKey(parentId)) {
			TsmpOrganization parentOrg = orgMap.get(parentId);
			return String.format(", Parent: %s (%s)", parentId, parentOrg.getOrgName());
		}
		return "";
	}
	
	/**
	 * 驗證是否有循環引用
	 */
	private void validateCircularReference(TsmpOrganization org, Map<String, TsmpOrganization> orgMap, List<String> errors) {
		List<String> circularPath = new ArrayList<>();
		if (hasCircularReference(org.getOrgId(), orgMap, new HashSet<>(), circularPath)) {
			// 建構循環路徑的詳細描述
			String pathDescription = buildCircularPathDescription(circularPath, orgMap);
			
			errors.add(String.format("Node %s (%s) has circular reference - Path: %s", 
					org.getOrgId(), org.getOrgName(), pathDescription));
		}
	}
	
	/**
	 * 建構循環路徑描述
	 */
	private String buildCircularPathDescription(List<String> circularPath, Map<String, TsmpOrganization> orgMap) {
		StringBuilder pathBuilder = new StringBuilder();
		for (int i = 0; i < circularPath.size(); i++) {
			String nodeId = circularPath.get(i);
			TsmpOrganization pathOrg = orgMap.get(nodeId);
			String nodeName = pathOrg != null ? pathOrg.getOrgName() : "unknown";
			pathBuilder.append(String.format("%s (%s)", nodeId, nodeName));
			if (i < circularPath.size() - 1) {
				pathBuilder.append(" -> ");
			}
		}
		return pathBuilder.toString();
	}
	
	/**
	 * 檢查是否有循環引用
	 * 
	 * @param orgId 當前節點ID
	 * @param orgMap 組織對應表
	 * @param visited 已訪問的節點集合
	 * @param path 循環路徑追蹤
	 * @return 是否有循環引用
	 */
	private boolean hasCircularReference(String orgId, Map<String, TsmpOrganization> orgMap, Set<String> visited, List<String> path) {
		path.add(orgId);
		
		if (visited.contains(orgId)) {
			return true; // 發現循環引用
		}
		
		TsmpOrganization org = orgMap.get(orgId);
		if (org == null || org.getParentId() == null || org.getParentId().trim().isEmpty()) {
			return false; // 到達根節點或找不到節點
		}
		
		visited.add(orgId);
		return hasCircularReference(org.getParentId(), orgMap, visited, path);
	}
	
	/**
	 * 格式化組織路徑，將每個節點 ID 加上對應的名稱
	 * 
	 * @param orgPath 組織路徑
	 * @param orgMap 組織對應表
	 * @return 格式化後的路徑，格式為 "ID (名稱)"
	 */
	private String formatOrgPathWithNames(String orgPath, Map<String, TsmpOrganization> orgMap) {
		if (orgPath == null || orgPath.trim().isEmpty()) {
			return orgPath;
		}
		
		String[] nodeIds = orgPath.split("::");
		StringBuilder formattedPath = new StringBuilder();
		
		for (int i = 0; i < nodeIds.length; i++) {
			String nodeId = nodeIds[i].trim();
			TsmpOrganization org = orgMap.get(nodeId);
			String nodeName = org != null ? org.getOrgName() : "unknown";
			
			formattedPath.append(String.format("%s (%s)", nodeId, nodeName));
			
			if (i < nodeIds.length - 1) {
				formattedPath.append("::");
			}
		}
		
		return formattedPath.toString();
	}

}
