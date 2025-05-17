package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0427Req;
import tpi.dgrv4.dpaa.vo.AA0427Resp;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0427Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;

	@Autowired
	public AA0427Service(TsmpApiDao tsmpApiDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
	}

	public AA0427Resp queryAllLabel(TsmpAuthorization authorization, AA0427Req req) {
		AA0427Resp resp = new AA0427Resp();
		try {
			List<String> apiSrc = req.getApiSrc();
			List<String> label1 = getTsmpApiDao().query_AA0427Lable1(apiSrc);
			List<String> label2 = getTsmpApiDao().query_AA0427Lable2(apiSrc);
			List<String> label3 = getTsmpApiDao().query_AA0427Lable3(apiSrc);
			List<String> label4 = getTsmpApiDao().query_AA0427Lable4(apiSrc);
			List<String> label5 = getTsmpApiDao().query_AA0427Lable5(apiSrc);
			Set<String> set = new HashSet<>();
			set.addAll(label1);
			set.addAll(label2);
			set.addAll(label3);
			set.addAll(label4);
			set.addAll(label5);
			set.removeIf(value -> !StringUtils.hasLength(value));
			List<String> labeList = new ArrayList<>(set);
			labeList.sort(null);
			if (labeList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setLabelList(labeList);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
}
