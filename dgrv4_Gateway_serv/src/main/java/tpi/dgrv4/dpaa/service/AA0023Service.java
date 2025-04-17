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
import tpi.dgrv4.dpaa.vo.AA0023Req;
import tpi.dgrv4.dpaa.vo.AA0023Resp;
import tpi.dgrv4.dpaa.vo.AA0023RoleInfo;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0023Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0023Service(TsmpRoleDao tsmpRoleDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0023Resp queryRoleRoleList(TsmpAuthorization authorization, AA0023Req req) {
		AA0023Resp aa0023_resp = new AA0023Resp();
		try {
			String roleId = req.getRoleId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String paging = req.getPaging();
			
			List<TsmpRole> roleList = null;

			if ("N".equals(paging)) {
				// 不分頁
				roleList = getTsmpRoleDao().queryByAA0023Service(roleId, authorization.getUserNameForQuery(), words,
						Integer.MAX_VALUE);
			} else {
				// 分頁
				roleList = getTsmpRoleDao().queryByAA0023Service(roleId, authorization.getUserNameForQuery(), words,
						getPageSize());
				if (roleList.size() == 0) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
			}
			
			//查詢結果放進AA0023Resp.roleRoleMappingList
			List<AA0023RoleInfo> roleRoleMappingList = new ArrayList<>();
			roleList.forEach(vo ->{
				AA0023RoleInfo infoVo = new AA0023RoleInfo();
				infoVo.setRoleId(vo.getRoleId());
				infoVo.setRoleName(vo.getRoleName());
				if(vo.getRoleAlias() == null) {
					infoVo.setRoleAlias("unknow:" + vo.getRoleName());
				}else {
					infoVo.setRoleAlias(vo.getRoleAlias());
				}
				roleRoleMappingList.add(infoVo);
			});
			
			aa0023_resp.setRoleRoleMappingList(roleRoleMappingList);
			
		} catch (TsmpDpAaException aa0023_e) {
			throw aa0023_e;
		} catch (Exception aa0023_e) {
			this.logger.error(StackTraceUtil.logStackTrace(aa0023_e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return aa0023_resp;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0023");
		return this.pageSize;
	}
}
