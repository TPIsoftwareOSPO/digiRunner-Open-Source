package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0104List;
import tpi.dgrv4.dpaa.vo.AA0104Req;
import tpi.dgrv4.dpaa.vo.AA0104Resp;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0104Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpRoleDao tsmpRoleDao;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;

	@Autowired
	public AA0104Service(TsmpRoleDao tsmpRoleDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0104Resp queryTFuncRoleList(AA0104Req req) {
		
		AA0104Resp resp =  new AA0104Resp();

		try {
			
			String funcCode = req.getFuncCode();
			String roleId = req.getRoleId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			// 1362:功能代碼:必填參數
			if(StringUtils.isEmpty(funcCode)) {
				throw TsmpDpAaRtnCode._1362.throwing();
			}
			
			List<TsmpRole> roleList =  getTsmpRoleDao().query_aa0104Service(roleId, funcCode, words, getPageSize());
			
			// 1298:查無資料
			if(roleList == null || roleList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();	
			}
				
			List<AA0104List> roleInfoList = getAA1004Resp(roleList);
			resp.setRoleInfoList(roleInfoList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<AA0104List> getAA1004Resp( List<TsmpRole> tsmpFuncList  ){
		List<AA0104List> list = new ArrayList<>();
		tsmpFuncList.forEach((tsmpRole) ->{
			AA0104List aa0104List = new AA0104List();
			aa0104List.setRoleAlias(ServiceUtil.nvl(tsmpRole.getRoleAlias()));
			aa0104List.setRoleId(ServiceUtil.nvl(tsmpRole.getRoleId()));
			aa0104List.setRoleName(ServiceUtil.nvl(tsmpRole.getRoleName()));
			
			list.add(aa0104List);
			
		});
		
		return list;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0104");
		return this.pageSize;
	}
	
}
