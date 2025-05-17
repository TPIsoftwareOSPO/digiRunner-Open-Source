package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1103Req;
import tpi.dgrv4.dpaa.vo.AA1103Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1103Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSecurityLevelDao tsmpSecurityLevelDao;

	@Autowired
	public AA1103Service(TsmpSecurityLevelDao tsmpSecurityLevelDao) {
		super();
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
	}

	public AA1103Resp updateSecurityLevel(TsmpAuthorization authorization, AA1103Req req) {
		AA1103Resp resp = new AA1103Resp();

		try {
			checkParam(req);

			String securityLevelId = req.getSecurityLevelId();
			String oriSecurityLevelName = req.getOriSecurityLevelName();
			String newSecurityLevelName = req.getNewSecurityLevelName();
			String newSecurityLevelDesc = req.getNewSecurityLevelDesc();

			TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelIdAndSecurityLevelName(securityLevelId,
					oriSecurityLevelName);
			
			vo.setSecurityLevelName(newSecurityLevelName);
			vo.setSecurityLevelDesc(newSecurityLevelDesc);

			getTsmpSecurityLevelDao().saveAndFlush(vo);

			resp.setSecurityLevelId(securityLevelId);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (ServiceUtil.isValueTooLargeException(e)) {
				// 1220:儲存失敗，資料長度過大
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				// 更新失敗
				throw TsmpDpAaRtnCode._1286.throwing();
			}

		}
		return resp;
	}

	private void checkParam(AA1103Req req) {
		String securityLevelId = req.getSecurityLevelId();
		String oriSecurityLevelName = req.getOriSecurityLevelName();
		String newSecurityLevelName = req.getNewSecurityLevelName();

		if (StringUtils.isEmpty(securityLevelId)) {
			// 缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		if (StringUtils.isEmpty(oriSecurityLevelName)) {
			// 缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelIdAndSecurityLevelName(securityLevelId,
				oriSecurityLevelName);
		if (vo == null) {
			// 查無資料
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		if (!oriSecurityLevelName.equals(newSecurityLevelName)) {
			vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelName(newSecurityLevelName);
			if (vo != null) {
				// [{{newSecurityLevelName}}] 不得重複
				throw TsmpDpAaRtnCode._1284.throwing("{{newSecurityLevelName}}");
			}
		}
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

}
