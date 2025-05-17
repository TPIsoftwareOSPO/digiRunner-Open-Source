package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0018Req;
import tpi.dgrv4.dpaa.vo.AA0018Resp;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0018Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	
	@Autowired
	public AA0018Service(TsmpRoleDao tsmpRoleDao, TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
	}

	@Transactional
	public AA0018Resp updateTRoleRoleMap(TsmpAuthorization authorization, AA0018Req req) {
		AA0018Resp resp = new AA0018Resp();

		try {
			checkParam(req);
			
			//1.新增資料到TSMP_ROLE_ROLE_MAPPING資料表，欄位對應ROLE_NAME=AA0018Req.roleName、ROLE_NAME_MAPPING=AA0018Req.roleNameMapping每一筆資料。
			String roleName = req.getRoleName();
			List<String> mappingList = req.getRoleNameMapping();
			List<TsmpRoleRoleMapping> tsmpRoleRoleMappList = getTsmpRoleRoleMappingDao().findByRoleName(roleName);

			if(mappingList != null) {
				
				// 將mappingList和tsmpRoleRoleMappList轉換為Set，以便快速檢查元素是否存在
				Set<String> mappingSet = new HashSet<>(mappingList);
				Set<String> existingRoleNameMappings = tsmpRoleRoleMappList.stream()
						.map(TsmpRoleRoleMapping::getRoleNameMapping)
						.collect(Collectors.toSet());
				
				// 找出需要新增的roleName
				List<String> toAdd = mappingList.stream()
				    .filter(roleNameMapping -> !existingRoleNameMappings.contains(roleNameMapping))
				    .collect(Collectors.toList());

				// 找出需要刪除的roleName
				List<String> toRemove = tsmpRoleRoleMappList.stream()
				    .map(TsmpRoleRoleMapping::getRoleNameMapping)
				    .filter(roleNameMapping -> !mappingSet.contains(roleNameMapping))
				    .collect(Collectors.toList());
				
				// 新增缺少的roleName
				List<TsmpRoleRoleMapping> newMappingList = new ArrayList<>();
				for (String roleNameMapping : toAdd) {
				    TsmpRoleRoleMapping newMapping = new TsmpRoleRoleMapping();
				    newMapping.setRoleName(roleName);
				    newMapping.setRoleNameMapping(roleNameMapping);
				    newMappingList.add(newMapping);
				}
				if (!CollectionUtils.isEmpty(newMappingList)) {
					getTsmpRoleRoleMappingDao().saveAll(newMappingList);					
				}

				// 刪除多餘的roleName
				for (String roleNameMapping : toRemove) {
				    getTsmpRoleRoleMappingDao().deleteByRoleNameAndRoleNameMapping(roleName, roleNameMapping);
				}
			}
			
			resp.setRoleName(roleName);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			//1286:更新失敗
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		return resp;
	}
	
	private void checkParam(AA0018Req req) {
		
		String roleName = req.getRoleName();
		List<String> mappingList = req.getRoleNameMapping();
		
		if(StringUtils.isEmpty(roleName)) {
			//1267:登入角色:必填參數
			throw TsmpDpAaRtnCode._1267.throwing();
		}
		
		if(roleName.length() > 50) {
			//1265:登入角色:長度限制 [{{0}}] 字內，長度[{{1}}] 個字
			throw TsmpDpAaRtnCode._1265.throwing("50", String.valueOf(roleName.length()));
		}
		
		if(mappingList != null) {
			mappingList.forEach(x->{
				if(x.length() > 50) {
					//1263:可授權角色:長度限制 [{{0}}] 字內，[{{1}}] 長度[{{2}}] 個字
					throw TsmpDpAaRtnCode._1263.throwing("50", x, String.valueOf(x.length()));
				}
			});
			
			
			//2.檢查AA0018Req.roleNameMapping是否存在，查詢TSMP_ROLE資料表，若不存在則throw RTN CODE 1262。
			mappingList.forEach(x->{
				TsmpRole checkTsmpRole = getTsmpRoleDao().findFirstByRoleName(x);
				if(checkTsmpRole == null) {
					//1262:可授權角色 [{{0}}] 不存在
					throw TsmpDpAaRtnCode._1262.throwing(x);
				}
				
			});
		}
		
		
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}

}
