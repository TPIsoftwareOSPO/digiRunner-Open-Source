package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherIfs;
import tpi.dgrv4.dpaa.vo.AA0705Req;
import tpi.dgrv4.dpaa.vo.AA0705Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleAlertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0705Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpAlertDao tsmpAlertDao;
	private TsmpRoleAlertDao tsmpRoleAlertDao;
    private DpaaAlertDispatcherIfs dpaaAlertDispatcher;

    @Autowired
	public AA0705Service(TsmpAlertDao tsmpAlertDao, TsmpRoleAlertDao tsmpRoleAlertDao,
			DpaaAlertDispatcherIfs dpaaAlertDispatcher) {
		super();
		this.tsmpAlertDao = tsmpAlertDao;
		this.tsmpRoleAlertDao = tsmpRoleAlertDao;
		this.dpaaAlertDispatcher = dpaaAlertDispatcher;
	}

	@Transactional
	public AA0705Resp deleteAlertSetting(TsmpAuthorization authorization, AA0705Req req) {
		AA0705Resp resp = new AA0705Resp();

		try {
			String alertId = req.getAlertID();
			String alertName = req.getAlertName();

			//檢查是否在TSMP_ALERT資料表有存在，若沒有查詢到資料，則throw 1429。
			TsmpAlert vo = getTsmpAlertDao().findFirstByAlertIdAndAlertName(Long.valueOf(alertId), alertName);
			if(vo == null) {
				//告警設定不存在
				throw TsmpDpAaRtnCode._1429.throwing();
			}

			//1.刪除TSMP_ROLE_ALERT資料表
			getTsmpRoleAlertDao().deleteByAlertId(Long.valueOf(alertId));
			
			//2.刪除TSMP_ALERT資料表
			getTsmpAlertDao().delete(vo);

			getDpaaAlertDispatcher().separateAlert(vo.getAlertId());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1287:刪除失敗
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}

	protected TsmpAlertDao getTsmpAlertDao() {
		return tsmpAlertDao;
	}

	protected TsmpRoleAlertDao getTsmpRoleAlertDao() {
		return tsmpRoleAlertDao;
	}

	protected DpaaAlertDispatcherIfs getDpaaAlertDispatcher() {
		return dpaaAlertDispatcher;
	}
}
