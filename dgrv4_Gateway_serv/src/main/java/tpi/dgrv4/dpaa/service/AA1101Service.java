package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1101Req;
import tpi.dgrv4.dpaa.vo.AA1101Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1101Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSecurityLevelDao tsmpSecurityLevelDao; 

	@Autowired
	public AA1101Service(TsmpSecurityLevelDao tsmpSecurityLevelDao) {
		super();
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
	}

	public AA1101Resp addSecurityLevel(TsmpAuthorization authorization, AA1101Req req) {
		AA1101Resp resp = new AA1101Resp();

		try {
			checkParam(req);
			
			String aa1101_securityLevelId = req.getSecurityLevelId();
			String securityLevelName = req.getSecurityLevelName();
			String securityLevelDesc = req.getSecurityLevelDesc();
			
			TsmpSecurityLevel aa1101_vo = new TsmpSecurityLevel();
			aa1101_vo.setSecurityLevelId(aa1101_securityLevelId);
			aa1101_vo.setSecurityLevelName(securityLevelName);
			aa1101_vo.setSecurityLevelDesc(securityLevelDesc);
			
			getTsmpSecurityLevelDao().saveAndFlush(aa1101_vo);
			
			resp.setSecurityLevelId(aa1101_securityLevelId);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException aa1101_e) {
			throw aa1101_e;
		} catch (Exception aa1101_e) {
			if(ServiceUtil.isValueTooLargeException(aa1101_e)) {
				//1220:儲存失敗，資料長度過大
				throw TsmpDpAaRtnCode._1220.throwing();
			}else {
				this.logger.error(StackTraceUtil.logStackTrace(aa1101_e));
				//新增失敗
				throw TsmpDpAaRtnCode._1288.throwing();
			}
			
		}
		return resp;
	}
	
	private void checkParam(AA1101Req req) {
		String securityLevelId = req.getSecurityLevelId();
		String securityLevelName = req.getSecurityLevelName();
		
		TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findById(securityLevelId).orElse(null);
		if(vo != null) {
			//[{{securityLevelId}}] 不得重複
			throw TsmpDpAaRtnCode._1284.throwing("{{securityLevelId}}");
		}
		
		vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelName(securityLevelName);
		if(vo != null) {
			//[{{securityLevelName}}] 不得重複
			throw TsmpDpAaRtnCode._1284.throwing("{{securityLevelName}}");
		}
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

}
