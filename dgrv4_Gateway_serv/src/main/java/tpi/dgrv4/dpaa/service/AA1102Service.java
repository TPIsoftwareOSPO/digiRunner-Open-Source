package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA1102Req;
import tpi.dgrv4.dpaa.vo.AA1102Resp;
import tpi.dgrv4.dpaa.vo.AA1102SecurityLevel;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1102Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSecurityLevelDao securityLVDao;

	@Autowired
	public AA1102Service(TsmpSecurityLevelDao securityLVDao) {
		super();
		this.securityLVDao = securityLVDao;
	}

	public AA1102Resp querySecurityLevel(TsmpAuthorization authorization, AA1102Req req) {
		AA1102Resp resp = new AA1102Resp();
		try {

			//查詢全部Security Level資料。
			List<TsmpSecurityLevel> securityLevelData = getSecurityLVDao().findAll();

			// 查無資料
			if (securityLevelData == null || securityLevelData.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			//轉換資料
			List<AA1102SecurityLevel> securityLevelList = convertAA1102SecurityLevel(securityLevelData);
			
			resp.setSecurityLevelList(securityLevelList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<AA1102SecurityLevel> convertAA1102SecurityLevel(List<TsmpSecurityLevel> securityLevelData) {

		List<AA1102SecurityLevel> aa1102SecurityLevelList = new ArrayList<AA1102SecurityLevel>();
		for (TsmpSecurityLevel securityLV : securityLevelData) {
			AA1102SecurityLevel aa1102SecurityLevel = new AA1102SecurityLevel();
			aa1102SecurityLevel.setSecurityLevelId(securityLV.getSecurityLevelId());
			aa1102SecurityLevel.setSecurityLevelName(securityLV.getSecurityLevelName());
			aa1102SecurityLevel.setSecurityLevelDesc(securityLV.getSecurityLevelDesc());
			aa1102SecurityLevelList.add(aa1102SecurityLevel);
		}
		
		return aa1102SecurityLevelList;
	}

	protected TsmpSecurityLevelDao getSecurityLVDao() {
		return securityLVDao;
	}

}
