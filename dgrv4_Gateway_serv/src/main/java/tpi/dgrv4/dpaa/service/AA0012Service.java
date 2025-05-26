package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0012Req;
import tpi.dgrv4.dpaa.vo.AA0012Resp;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0012Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRoleFuncDao tsmpRoleFuncDao;
	
	@Autowired
	public AA0012Service(TsmpRoleFuncDao tsmpRoleFuncDao) {
		super();
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
	}

	public AA0012Resp queryFuncByLoginUser(TsmpAuthorization auth, AA0012Req req) {
		AA0012Resp resp = new AA0012Resp();
		resp.setFuncCodeList(new ArrayList<String>());
		try {
			List<TsmpRoleFunc> data = getTsmpRoleFuncDao().queryByUserName(auth.getUserNameForQuery());

			if (data != null && data.isEmpty() == false) {
				List<String> funcCodeList = data.stream().map(f -> f.getFuncCode()).collect(Collectors.toList());
				resp.setFuncCodeList(funcCodeList);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected TsmpRoleFuncDao getTsmpRoleFuncDao() {
		return tsmpRoleFuncDao;
	}

}
