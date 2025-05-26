package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0060Req;
import tpi.dgrv4.dpaa.vo.DPB0060Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0060Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public DPB0060Service(TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0060Resp cancelJobByPk(TsmpAuthorization auth, DPB0060Req req, ReqHeader reqHeader) {
		// 檢查必要參數
		final Long apptJobId = req.getApptJobId();
		final Long lv = req.getLv();
		if (apptJobId == null || lv == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(apptJobId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		final TsmpDpApptJob job = opt.get();
		final String status = job.getStatus();
		if (!TsmpDpApptJobStatus.WAIT.isValueEquals(status)) {
			this.logger.debug("狀態非等待: " + status);
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		/* 
		 * 原本 execOwner 是放 auth.getUserName(),
		 * 但若是以 IdP 登入 AC 時會過長, ex. GOOGLE.李OO Mini Lee, 
		 * 故改為 "MANUAL_CANCEL"
		 * 實際操作者可看 updateUser 欄位
		 */
		String manualCancel = "MANUAL_CANCEL"; // 手動取消
		
		TsmpDpApptJob nJob = ServiceUtil.deepCopy(job, TsmpDpApptJob.class);
		nJob.setStatus(TsmpDpApptJobStatus.CANCEL.value());
		nJob.setExecResult(manualCancel);
		nJob.setExecOwner(manualCancel);
		nJob.setUpdateDateTime(DateTimeUtil.now());
		nJob.setUpdateUser(auth.getUserName());
		nJob.setVersion(lv);
		try {
			nJob = getTsmpDpApptJobDao().saveAndFlush(nJob);
		} catch (ObjectOptimisticLockingFailureException e) {
			this.logger.error(String.format("expected version: %d but %d", job.getVersion(), lv));
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		}
		
		DPB0060Resp resp = new DPB0060Resp();
		resp.setApptJobId(nJob.getApptJobId());
		String statusText = getStatusText(nJob.getStatus(), reqHeader.getLocale());
		resp.setStatus(statusText);
		resp.setLv(nJob.getVersion());
		return resp;
	}

	private String getStatusText(String status, String locale) {
		if (!StringUtils.isEmpty(status)) {
			TsmpDpItems items = getItemsById("JOB_STATUS", status, false, locale);
			if (items != null) {
				return status.concat("：").concat(items.getSubitemName());
			}
		}
		return new String();
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems dpb0060_vo= getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists) {
			if(dpb0060_vo != null) {
				return dpb0060_vo;
			}else {
				throw TsmpDpAaRtnCode._1297.throwing();
			}

		} else {
			return dpb0060_vo;
		}
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

}
