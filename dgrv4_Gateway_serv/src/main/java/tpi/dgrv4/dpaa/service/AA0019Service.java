package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0019List;
import tpi.dgrv4.dpaa.vo.AA0019Req;
import tpi.dgrv4.dpaa.vo.AA0019Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0019Service {

	private TPILogger logger = TPILogger.tl;
	
	private Integer pageSize;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private UsersDao usersDao;
	private AuthoritiesDao authoritiesDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpUserDao tsmpUserDao;
	private ServiceConfig serviceConfig;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	@Autowired
	public AA0019Service(TsmpOrganizationDao tsmpOrganizationDao, UsersDao usersDao, AuthoritiesDao authoritiesDao,
			TsmpRoleDao tsmpRoleDao, TsmpUserDao tsmpUserDao, ServiceConfig serviceConfig,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.usersDao = usersDao;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpUserDao = tsmpUserDao;
		this.serviceConfig = serviceConfig;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}
	@Transactional
	public AA0019Resp queryTUserList(TsmpAuthorization auth, AA0019Req req, ReqHeader reqHeader) {
		AA0019Resp resp = null;

		// 檢查orgId是否存在，在TSMP_ORGANIZATION的ORG_ID欄位用orgId查詢，若查不到就拋出 1229:
		// 3.在步驟2中有找到資料，將資料中的ORG_PATH欄位帶入下列SQL，找出特定某個組織以下全部的節點組織，此SQL會合併到步驟4，不用單獨執行。
		// select ORG_ID,ORG_NAME from TSMP_ORGANIZATION where org_path like :ORG_PATH+""%""
		// 例如：如要找出右圖aa以下的節點，就將aa的ORG_PATH放進SQL，就可以找到aa與bb節點。
		try {
			String orgId = auth.getOrgId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String orgName = ServiceUtil.nvl(req.getOrgName());
			String roleName = ServiceUtil.nvl(req.getRoleName());
			String userId = req.getUserId();
			
			if (!StringUtils.isEmpty(orgId)) {

				Optional<TsmpOrganization> optional = getTsmpOrganizationDao().findById(orgId);

				if (optional.isPresent()) {
					// 找出使用者選取的組織orgIdList<由orgName找orgId>
					
					List<String> orgIdListFromOrgName = orgIdListFromOrgName(orgName);
					
					// 自己本身組織(含底下的)
					List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId,
							Integer.MAX_VALUE);
					
					resp = queryTUserList(roleName, orgIdListFromOrgName, orgDescList, words, userId, reqHeader.getLocale());
				} else {
					throw TsmpDpAaRtnCode._1229.throwing();
				}
				
			}else {
				logger.debug("AA0019Service queryTUserList orgId is null");
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	public AA0019Resp queryTUserList(String roleName, List<String> orgIdListFromOrgName, List<String> orgDescList,  String[] words, String userId, String locale) {
		AA0019Resp resp = null;
		
		List<TsmpUser> userList = getTsmpUserDao().query_aa0019Service(roleName, orgIdListFromOrgName, orgDescList, words, userId,
				getPageSize());
		
		if(userList != null && userList.size() > 0) {
			resp = new AA0019Resp();
			List<AA0019List> dataList = getAA0019List(userList, locale);
			resp.setUserInfoList(dataList);
			
		}else {
			//1298:查無資料
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		return resp;
	}
	
	private List<AA0019List> getAA0019List(List<TsmpUser> userList, String locale) {
		List<AA0019List> dataList = new ArrayList<AA0019List>();
		for (TsmpUser tsmpUser : userList) {
			AA0019List vo = getAA0019List(tsmpUser, locale);
			dataList.add(vo);
		}
		return dataList;
	}
	
	private AA0019List getAA0019List(TsmpUser tsmpUser, String locale){
		AA0019List data = new AA0019List();
		String orgName ="";
		String userName = "";
		List<String> roleList = new ArrayList<>();
		
		Optional<TsmpOrganization> orgOpt = getTsmpOrganizationDao().findById(tsmpUser.getOrgId());
		if(orgOpt.isPresent())
			orgName = orgOpt.get().getOrgName();
		
		String status = getItemsParam("ENABLE_FLAG", tsmpUser.getUserStatus(), locale);
		
		userName = tsmpUser.getUserName();
		
		//迴圈裡面再跑query 資料庫 ->效率會不好,但是因為有做分頁所以跑的資料量不會太多+JAP有一級快取的功能所以在分頁做此動作可接受
		//一級快取 -> 在還沒有commit之前 有搜尋過的資料會存在快取
		List<Authorities> authUserName = getAuthoritiesDao().findByUsername(userName);
		authUserName.forEach((auth) ->{
			String authId = auth.getAuthority();
			Optional<TsmpRole> optRole = getTsmpRoleDao().findById(authId);
			if(optRole.isPresent()) {
				String roleAlias = optRole.get().getRoleAlias();
				if(!StringUtils.isEmpty(roleAlias)) {
					roleList.add(roleAlias);
					
				}else {
					String msg = "unknow:" + optRole.get().getRoleName();
					roleList.add(msg);
				}
			}
		});
		
		data.setOrgId(tsmpUser.getOrgId());
		data.setOrgName(orgName);
		data.setRoleAlias(roleList);	//角色名稱
		data.setStatus(tsmpUser.getUserStatus());
		data.setStatusName(status);
		data.setUserAlias(tsmpUser.getUserAlias());	
		data.setUserID(tsmpUser.getUserId());
		data.setUserName(tsmpUser.getUserName());
		
		return data;
	}
	
	private String getItemsParam(String itemNo, String param1, String locale) {

		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale(itemNo, param1, locale);
		if (dpItem == null) {
			return null;
		}
		String subitemName = dpItem.getSubitemName();
		return subitemName;
	}
	
	private List<String> orgIdListFromOrgName(String orgName){
		List<String> orgIdListFromOrgName = new ArrayList<>();
		
		List<String> orgIdList = getTsmpOrganizationDao().findByOrgName(orgName, Integer.MAX_VALUE);
		orgIdList.forEach((orgIdFromOrgName) -> {
			List<String> orgIdTempList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgIdFromOrgName, Integer.MAX_VALUE);
			
			orgIdTempList.forEach((o) -> {
				if(!orgIdListFromOrgName.contains(o)) {
					orgIdListFromOrgName.add(o);
				}
			});
			
		});
		
		return orgIdListFromOrgName;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	

	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
	
	protected UsersDao getUsersDao() {
		return this.usersDao;
	}
	
	protected  Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0019");
		return this.pageSize;
	}
	
}
