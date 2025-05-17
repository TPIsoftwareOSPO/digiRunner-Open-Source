package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0108FuncInfo;
import tpi.dgrv4.dpaa.vo.AA0108Resp;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0108Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	
	@Autowired
	public AA0108Service(TsmpFuncDao tsmpFuncDao) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
	}

	public AA0108Resp queryCusMasterFunc(ReqHeader reqHeader ) {
		List<AA0108FuncInfo> list = new ArrayList<>();
		AA0108Resp resp = new AA0108Resp();
		try {
			String locale = reqHeader.getLocale();	
			List<TsmpFunc> funcList = getTsmpFuncDao().findByLocaleAndFuncTypeAndFuncCodeStartsWithOrderByFuncCodeAsc(locale, "1","ZA");

			funcList.forEach((tsmpFunc) ->{				
				if(tsmpFunc.getFuncCode().length() == 4) {
					AA0108FuncInfo aa0108FuncInfo = new AA0108FuncInfo();
					aa0108FuncInfo.setFuncCode(tsmpFunc.getFuncCode());
					aa0108FuncInfo.setFuncName(tsmpFunc.getFuncName());
					list.add(aa0108FuncInfo);
				}							
			});
			resp.setFuncInfoList(list);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing(); // 執行錯誤
		}
		return resp;
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return this.tsmpFuncDao;
	}
}
