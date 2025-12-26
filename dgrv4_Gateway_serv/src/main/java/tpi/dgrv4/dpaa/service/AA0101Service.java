package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0101Req;
import tpi.dgrv4.dpaa.vo.AA0101Resp;
import tpi.dgrv4.dpaa.vo.AA0101func;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0101Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;

	@Autowired
	public AA0101Service(TsmpFuncDao tsmpFuncDao) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
	}

	public AA0101Resp queryAllFunc(TsmpAuthorization auth, AA0101Req req, ReqHeader reqHeader) {
		AA0101Resp resp = new AA0101Resp();

		resp.setFuncList(new ArrayList<AA0101func>());

		try {
			List<TsmpFunc> data = getTsmpFuncDao().findByLocaleOrderByFuncCodeAsc(reqHeader.getLocale());
			
			//該語系查不到資料就查英文語系
			if(CollectionUtils.isEmpty(data)) {
				data = getTsmpFuncDao().findByLocaleOrderByFuncCodeAsc(LocaleType.EN_US);
			}

			List<String> removedList = new LinkedList<String>(); // 移除的 funcCode
			if (data != null && data.isEmpty() == false) {
				List<AA0101func> funcList = new LinkedList<AA0101func>();
				for (TsmpFunc tsmpFunc : data) {
					String funcCode = tsmpFunc.getFuncCode();
					if (isRemove(funcCode)) {// 要移除
						removedList.add(funcCode);
					} else {// 不移除
						AA0101func f = new AA0101func();
						f.setFuncCode(funcCode);
						f.setFuncName(tsmpFunc.getFuncName());
						f.setFuncURL(tsmpFunc.getFuncUrl());
						funcList.add(f);
					}
				}

				resp.setFuncList(funcList);
			}
			
			if (!CollectionUtils.isEmpty(removedList)) {
				this.logger.debug("Removed v3 menu funCode:\n" + removedList);// 已移除的 v3 menu funCode
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();

		}

		return resp;
	}

	/**
	 * 判斷 funcCode 是否要移除(不顯示在畫面上) <br>
	 * 若是以下開頭的,則要移除 <br>
	 * AC04 - JAVA MODULE管理 <br>
	 * AC08 - 註冊主機管理 <br>
	 * AC11 - 查詢核身方式 <br>
	 * NP01 - 入口網後台管理 <br>
	 * AC0318 - API列表-匯入註冊/組合API <br>
	 * LB0004 - Customer Setting <br>
	 */
	private boolean isRemove(String funcCode) {
		String[] v3MemuList = new String[] { "AC04", "AC08", "AC1107", "NP01", "AC0318", "LB0004" };// 要移除的 funCode 開頭清單

		for (String str : v3MemuList) {
			if (funcCode.startsWith(str)) {
				return true;
			}
		}

		return false;
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return tsmpFuncDao;
	}

}
