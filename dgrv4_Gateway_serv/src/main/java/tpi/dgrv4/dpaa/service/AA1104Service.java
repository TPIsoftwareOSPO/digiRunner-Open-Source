package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA1104Req;
import tpi.dgrv4.dpaa.vo.AA1104Resp;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1104Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSecurityLevelDao tsmpSecurityLevelDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientDao tsmpClientDao;

	@Autowired
	public AA1104Service(TsmpSecurityLevelDao tsmpSecurityLevelDao, TsmpGroupDao tsmpGroupDao,
			TsmpClientDao tsmpClientDao) {
		super();
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientDao = tsmpClientDao;
	}

	public AA1104Resp deleteSecurityLevel(TsmpAuthorization authorization, AA1104Req req) {
		AA1104Resp resp = new AA1104Resp();

		try {
			checkParam(req);
			
			String securityLevelId = req.getSecurityLevelId();
			String securityLevelName = req.getSecurityLevelName();
			
			TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelIdAndSecurityLevelName(securityLevelId, securityLevelName);
			getTsmpSecurityLevelDao().delete(vo);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//刪除失敗
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}
	
	private void checkParam(AA1104Req req) {
		String securityLevelId = req.getSecurityLevelId();
		String securityLevelName = req.getSecurityLevelName();
		
		if(StringUtils.isEmpty(securityLevelId)) {
			//缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if(StringUtils.isEmpty(securityLevelName)) {
			//缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelIdAndSecurityLevelName(securityLevelId, securityLevelName);
		if(vo == null) {
			//查無資料
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		//檢查 群組 是否有附加該安全等級
		List<TsmpGroup> groupList = getTsmpGroupDao().findBySecurityLevelId(securityLevelId);
		if(groupList.size() > 0) {
			List<String> groupIdList =  groupList.stream().map(groupVo -> {
				return groupVo.getGroupId();
			}).collect(Collectors.toList());
			
			//此安全等級有未刪除的Group: {{0}}
			throw TsmpDpAaRtnCode._1482.throwing(groupIdList.toString());
		}
		
		//檢查 Client 是否有附加該安全等級
		List<TsmpClient> clientList = getTsmpClientDao().findBySecurityLevelId(securityLevelId);
		if(clientList.size() > 0) {
			List<String> clientIdList =  clientList.stream().map(clientVo -> {
				return clientVo.getClientId();
			}).collect(Collectors.toList());
			
			//此安全等級有未刪除的Client: {{0}}
			throw TsmpDpAaRtnCode._1483.throwing(clientIdList.toString());
		}
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

}
