package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0020List;
import tpi.dgrv4.dpaa.vo.AA0020Req;
import tpi.dgrv4.dpaa.vo.AA0020Resp;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0020Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0020Service(TsmpRoleDao tsmpRoleDao, TsmpRoleFuncDao tsmpRoleFuncDao,
			TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0020Resp queryTRoleList(TsmpAuthorization authorization, AA0020Req req) {
		
		AA0020Resp resp = null;

		try {
			String roleId = req.getRoleId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");

			List<TsmpRole> dataList = getTsmpRoleDao().findByRoleIdAndKeyword(roleId, words, getPageSize());

			if (dataList == null || dataList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<AA0020List> roleDetailList = getRoleDetailList(dataList, req);
			
			resp = new AA0020Resp();
			resp.setRoleDetailList(roleDetailList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<AA0020List> getRoleDetailList(List<TsmpRole> dataList, AA0020Req req) {

		List<AA0020List> aa0020List = new ArrayList<AA0020List>();

		dataList.forEach(data -> {

			AA0020List aa0020 = new AA0020List();
			aa0020.setRoleID(data.getRoleId());//角色代碼
			aa0020.setRoleName(data.getRoleName());//角色名稱
			//若TSMP_ROLE.ROLE_ALIAS欄位為null，則"unknow:" + ROLE_NAME欄位。
			if(data.getRoleAlias() == null) {
				aa0020.setRoleAlias("unknow:" + data.getRoleName());//角色代號
			}else {
				aa0020.setRoleAlias(data.getRoleAlias());//角色代號
			}
			
			//若funcFlag = true則將AA0020List.roleID對TSMP_ROLE_FUNC資料表(ROLE_ID欄位)查詢
			//，有多筆TSMP_ROLE_FUNC.FUNC_CODE資料再放進AA0020List.funcCodeList
			if(req.isFuncFlag()) {
				List<TsmpRoleFunc> funcList = getTsmpRoleFuncDao().findByRoleId(data.getRoleId());
				aa0020.setFuncCodeList(funcList.stream().map((f)->{
					return f.getFuncCode();
				}).collect(Collectors.toList()));//角色與功能關係
			}else {
				aa0020.setFuncCodeList(new ArrayList<String>());
			}
			
			//若authorityFlag = true則對TSMP_ROLE_ROLE_MAPPING資料表查詢
			//，條件ROLE_NAME=AA0020Req.roleName AND ROLE_NAME_MAPPING =AA0020List.roleName每一筆
			//，若有找到將AA0020List.mappingFlag = true
			if(req.isAuthorityFlag()) {
				List<TsmpRoleRoleMapping> roleRoleList = getTsmpRoleRoleMappingDao().findByRoleNameAndRoleNameMapping(req.getRoleName(), aa0020.getRoleName());
				if(roleRoleList != null && roleRoleList.size() > 0) {
					aa0020.setMappingFlag(true);
				}
			}
			
			aa0020List.add(aa0020);
		});

		return aa0020List;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0020");
		return this.pageSize;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpRoleFuncDao getTsmpRoleFuncDao() {
		return tsmpRoleFuncDao;
	}

	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}

}
