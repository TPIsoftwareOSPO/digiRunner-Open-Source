package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0215Req;
import tpi.dgrv4.dpaa.vo.AA0215Resp;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0215Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public AA0215Service(TsmpGroupDao tsmpGroupDao, TsmpClientGroupDao tsmpClientGroupDao,
			TsmpGroupApiDao tsmpGroupApiDao, TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao,
			DgrAuditLogService dgrAuditLogService, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Transactional
	public AA0215Resp deleteGroup(TsmpAuthorization authorization, AA0215Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_GROUP.value());
		
		AA0215Resp resp = new AA0215Resp();

		try {
			
			checkParam(req);
			
			String groupId = req.getGroupID();
			
			//2.刪除GROUP與API關聯資料，資料表為TSMP_GROUP_API。
			List<TsmpGroupApi> apiList = getTsmpGroupApiDao().findByGroupId(groupId);
			getTsmpGroupApiDao().deleteAll(apiList);
			apiList.forEach(vo ->{
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
				//寫入 Audit Log D
				String lineNumber2 = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber2, 
						TsmpGroupApi.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
			});
			
			//3.刪除群組核身對應資料，資料表為TSMP_GROUP_AUTHORITIES_MAP
			List<TsmpGroupAuthoritiesMap> authList = getTsmpGroupAuthoritiesMapDao().findByGroupId(groupId);
			getTsmpGroupAuthoritiesMapDao().deleteAll(authList);
			authList.forEach(vo ->{
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
				//寫入 Audit Log D
				String lineNumber2 = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber2, 
						TsmpGroupAuthoritiesMap.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
			});
			
			//4.刪除GROUP資料，資料表為TSMP_GROUP。
			TsmpGroup groupVo = getTsmpGroupDao().findById(groupId).orElse(null);
			if(groupVo != null) {
				getTsmpGroupDao().delete(groupVo);
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, groupVo); //舊資料統一轉成 String
				//寫入 Audit Log D
				String lineNumber2 = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber2, 
						TsmpGroup.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1287:刪除失敗
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}

	private void checkParam(AA0215Req req) {
		String groupId = req.getGroupID();
		boolean settingFlag = getTsmpSettingService().getVal_DEFAULT_DATA_CHANGE_ENABLED();
		
		if("1000".equals(groupId) && !settingFlag)
			throw TsmpDpAaRtnCode._1548.throwing();
		
		//1.檢查group是否存在，查詢TSMP_GROUP資料表。
		TsmpGroup tsmpGroupVo = getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(groupId, "0");
		if(tsmpGroupVo == null) {
			//1360:群組不存在
			throw TsmpDpAaRtnCode._1360.throwing();
		}
		
		//2.檢查group是否有被client使用
		List<TsmpClientGroup> clientGroupList = getTsmpClientGroupDao().findByGroupId(groupId);
		if(clientGroupList.size() > 0) {
			//1417:該群組有用戶端
			throw TsmpDpAaRtnCode._1417.throwing();
		}
	}


	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
