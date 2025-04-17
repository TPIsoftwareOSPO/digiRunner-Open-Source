package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0022Detail;
import tpi.dgrv4.dpaa.vo.AA0022List;
import tpi.dgrv4.dpaa.vo.AA0022Req;
import tpi.dgrv4.dpaa.vo.AA0022Resp;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0022Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	private AuthoritiesDao authoritiesDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0022Service(TsmpRoleDao tsmpRoleDao, TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao,
			AuthoritiesDao authoritiesDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
		this.authoritiesDao = authoritiesDao;
		this.serviceConfig = serviceConfig;
	}

	/**
	 * 1.若AA0022Req.roleFlag為true進行步驟2查詢，反之則進行步驟3查詢。
	 * 2.用authorization.userName將TSMP_ROLE、AUTHORITIES與TSMP_USER資料表進行關聯，並用關鍵字搜尋 + 分頁，找出TSMP_ROLE資料。
	 * 3.TSMP_ROLE、AUTHORITIES與TSMP_USER資料表進行關聯，用關鍵字搜尋 + 分頁，找出TSMP_ROLE資料。
	 * 4.將步驟2或是步驟3找到的TSMP_ROLE資料轉換成AA0022Resp.roleRoleMappingList。
	 * 5.查詢TSMP_ROLE_ROLE_MAPPING資料表，條件ROLE_NAME=AA0022Resp.roleRoleMappingList.roleName，查詢出ROLE_NAME_MAPPING欄位。
	 * 6.將步驟5的ROLE_NAME_MAPPING欄位資料，查詢TSMP_ROLE資料表，條件ROLE_NAME=ROLE_NAME_MAPPING。查詢到的TSMP_ROLE資料再放進AA0022Resp.roleRoleMappingList.roleRoleMapping內。
	 * 7.若AA0022Req.roleFlag為true則查詢AUTHORITIES資料表，條件為USERNAME=AA0022Req.userName與AA0022List.roleId，有找到資料則AA0022Resp.AA0022Resp.mappingFlag=true。
	 * */
	public AA0022Resp queryTRoleRoleMap(TsmpAuthorization authorization, AA0022Req req) {
		AA0022Resp resp = new AA0022Resp();
		try {
			String roleId = req.getRoleId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			List<TsmpRole> roleList = getTsmpRoleDao().findByAA0022Service(roleId, words, getPageSize());
			
			if(roleList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//4.將步驟2或是步驟3找到的TSMP_ROLE資料轉換成AA0022Resp.roleRoleMappingList
			List<AA0022List> roleRoleMappingList = new ArrayList<>();
			roleList.forEach(x->{
				AA0022List vo = new AA0022List();
				vo.setRoleId(x.getRoleId());
				vo.setRoleName(x.getRoleName());
				if(x.getRoleAlias() == null) {
					vo.setRoleAlias("unknow:" + x.getRoleName());
				}else {
					vo.setRoleAlias(x.getRoleAlias());
				}

				roleRoleMappingList.add(vo);
			});
			
			
			//5.查詢TSMP_ROLE_ROLE_MAPPING資料表，條件ROLE_NAME=AA0022Resp.roleRoleMappingList.roleName，查詢出ROLE_NAME_MAPPING欄位。
			roleRoleMappingList.forEach(x->{
				List<TsmpRoleRoleMapping> mappingList = getTsmpRoleRoleMappingDao().findByRoleName(x.getRoleName());
				//6.將步驟5的ROLE_NAME_MAPPING欄位資料，查詢TSMP_ROLE資料表，條件ROLE_NAME=ROLE_NAME_MAPPING。
				//  查詢到的TSMP_ROLE資料再放進AA0022Resp.roleRoleMappingList.roleRoleMapping內。
				List<AA0022Detail> aa0022DetailList = new ArrayList<>();
				x.setRoleRoleMapping(aa0022DetailList);
				List<String> aliasList = new ArrayList<>();
				mappingList.forEach(m->{
					List<TsmpRole> tsmpRoleList = getTsmpRoleDao().findByRoleName(m.getRoleNameMapping());
					tsmpRoleList.forEach(r->{
						AA0022Detail vo = new AA0022Detail();
						vo.setRoleId(r.getRoleId());
						vo.setRoleName(r.getRoleName());
						if(r.getRoleAlias() == null) {
							vo.setRoleAlias("unknow:" + r.getRoleName());
						}else {
							vo.setRoleAlias(r.getRoleAlias());
						}
						aa0022DetailList.add(vo);
						aliasList.add(vo.getRoleAlias());
					});
					
					//截斷後的"可授權角色清單"和完整的"可授權角色清單"
					String oriRoleRoleMappingInfo = String.join("，", aliasList);
					String roleRoleMappingInfo = null;
					if(oriRoleRoleMappingInfo.length() > 100) {
						roleRoleMappingInfo = oriRoleRoleMappingInfo.substring(0, 100) + "...";
						x.setMsgTruncated(true);
					}else {
						roleRoleMappingInfo = oriRoleRoleMappingInfo;
						x.setMsgTruncated(false);
					}
					x.setOriRoleRoleMappingInfo(oriRoleRoleMappingInfo);
					x.setRoleRoleMappingInfo(roleRoleMappingInfo);
					
				});
				
			});
			
			resp.setRoleRoleMappingList(roleRoleMappingList);
			
		} catch (TsmpDpAaException aa0022_e) {
			throw aa0022_e;
		} catch (Exception aa0022_e) {
			this.logger.error(StackTraceUtil.logStackTrace(aa0022_e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0022");
		return this.pageSize;
	}
}
