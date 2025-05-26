package tpi.dgrv4.gateway.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.TCP.Packet.NotifyClientRefreshMemListPacket;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.DPB0059Req;
import tpi.dgrv4.gateway.vo.DPB0059Resp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
@Transactional
public class DPB0059Service {
    

	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ApptJobDispatcher apptJobDispatcher;
    
	@Autowired
    public DPB0059Service(TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ApptJobDispatcher apptJobDispatcher) {
		super();
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.apptJobDispatcher = apptJobDispatcher;
	}
	protected void refreshMemList() {
        try {

            synchronized (TPILogger.lc) {//
                TPILogger.lc.send(new NotifyClientRefreshMemListPacket());
            }
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode.SYSTEM_ERROR.throwing();
        }

    }
	public DPB0059Resp doJobByPk(TsmpAuthorization auth, DPB0059Req req, ReqHeader reqHeader) {
	    
		// 檢查必要參數
		final Long apptJobId = req.getApptJobId();
		if (apptJobId == null) {
			throw DgrRtnCode._1297.throwing();
		}
//		DPB0059Resp resp=	doJobByPk(apptJobId,auth.getUserName(),reqHeader.getLocale());
//		  return resp; 
		Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(apptJobId);
		if (!opt.isPresent()) {
			throw DgrRtnCode._1298.throwing();
		}
		TsmpDpApptJob job = opt.get();
		
		// 若無法依 refItemNo + refSubitemNo 找到 ApptJob bean 就報錯
		String beanName = null;
		try {
			beanName = getApptJobDispatcher().getBeanName(job);
			getApptJobDispatcher().getBeanByName(beanName, job);
		} catch (Exception e) {
			throw DgrRtnCode._1491.throwing(beanName);
		}

	
		// 決定要重做還是執行
		final String status = job.getStatus();
		if (
			TsmpDpApptJobStatus.ERROR.isValueEquals(status) ||
			TsmpDpApptJobStatus.DONE.isValueEquals(status) ||
			TsmpDpApptJobStatus.RUNNING.isValueEquals(status)
		) {
			job = redo(auth, job, false);
		} else if (
			TsmpDpApptJobStatus.WAIT.isValueEquals(status) ||
			TsmpDpApptJobStatus.CANCEL.isValueEquals(status)
		) {
			job = redo(auth, job, true);
		} else {
			throw DgrRtnCode._1297.throwing();
		}
	    refreshMemList();
		DPB0059Resp resp = null;
		try {
		
		
			resp = getDpb0059Resp(job, reqHeader.getLocale());
		} catch (Exception e) {
			throw DgrRtnCode._1298.throwing();
		}
		return resp;
	}

	private TsmpDpApptJob redo(TsmpAuthorization auth, TsmpDpApptJob job //
			, boolean isCancelOrig) {
		// 重新複製一筆新的
		TsmpDpApptJob newJob = copyJob(auth, job, isCancelOrig);
		
		// 如果是"執行", 則取消原本的工作, 並在執行結果備註"手動執行"
		if (isCancelOrig) {
			// 為了避免手動執行的同時, 排程器已拿去執行, 故加入 Version 的檢核
			TsmpDpApptJob copiedJob = ServiceUtil.deepCopy(job, TsmpDpApptJob.class);
			copiedJob.setStatus(TsmpDpApptJobStatus.CANCEL.value());
			copiedJob.setExecResult("MANUAL_EXEC");//"手動執行"
			copiedJob.setUpdateDateTime(DateTimeUtil.now());
			copiedJob.setUpdateUser(auth.getUserName());
			try {
				copiedJob = getTsmpDpApptJobDao().save(copiedJob);
			} catch (ObjectOptimisticLockingFailureException e) {
				throw DgrRtnCode._1191.throwing();
			}
		}
		
		return getApptJobDispatcher().addAndRefresh(newJob);
	}

	private TsmpDpApptJob copyJob(TsmpAuthorization auth, TsmpDpApptJob job, boolean isCancelOrig) {
		/* 
		 * 原本 execOwner 是放 auth.getUserName(),
		 * 但若是以 IdP 登入 AC 時會過長, ex. GOOGLE.李OO Mini Lee, 
		 * 故改為 "MANUAL_CANCEL"
		 * 實際操作者可看 createUser 欄位
		 */
		String manualCancel = "MANUAL_CANCEL"; // 手動取消
		
		TsmpDpApptJob newJob = new TsmpDpApptJob();
		newJob.setRefItemNo(job.getRefItemNo());
		newJob.setRefSubitemNo(job.getRefSubitemNo());
		newJob.setStatus(TsmpDpApptJobStatus.WAIT.value());
		newJob.setInParams(job.getInParams());
		newJob.setExecOwner(manualCancel);
		newJob.setStartDateTime(DateTimeUtil.now());
		newJob.setFromJobId(job.getApptJobId());
		newJob.setCreateUser(auth.getUserName());
		newJob.setIdentifData("apptJobId=" + job.getApptJobId());//來自工作ID
		
		// 是週期排程
		if (
			!StringUtils.isEmpty(job.getPeriodUid()) &&
			job.getPeriodItemsId() != null &&
			job.getPeriodNexttime() != null
		) {
			newJob.setPeriodUid(job.getPeriodUid());
			newJob.setPeriodItemsId(job.getPeriodItemsId());
			newJob.setPeriodNexttime(job.getPeriodNexttime());			
			newJob.setIdentifData(job.getIdentifData());
			
			// 因為 (periodUid + periodItemsId + periodNexttime) 是 UK，所以複製出來的 periodNexttime 不能跟舊的一樣
			boolean hasSamePeriod = false;
			do {
				// 偷偷加1
				newJob.setPeriodNexttime( newJob.getPeriodNexttime() + 1 );
				TPILogger.tl.debug("手動執行週期排程的工作, 遞增時間: " + job.getPeriodNexttime() + " -> " + newJob.getPeriodNexttime());
				
				TsmpDpApptJob periodJob = getApptJobDispatcher().getPeriodJob(//
					newJob.getPeriodUid(), newJob.getPeriodItemsId(), newJob.getPeriodNexttime());
				hasSamePeriod = (periodJob != null);
			} while(hasSamePeriod);
		}
 
		return newJob;
	}

	private DPB0059Resp getDpb0059Resp(TsmpDpApptJob job, String locale) {
		DPB0059Resp dpb0059Resp = new DPB0059Resp();
		
		String refItemNo = getRefItemNo(job.getRefItemNo(), locale);
		String refSubitemNo = getRefSubitemNo(job.getRefItemNo(), job.getRefSubitemNo(), locale);
		String status = getStatusText(job.getStatus(), locale);
		String startDateTime = getJobDateTime(job.getStartDateTime());
		String jobStep = nvl(job.getJobStep());
		String execResult = nvl(job.getExecResult());
		String createDateTime = getJobDateTime(job.getCreateDateTime());
		String createUser = nvl(job.getCreateUser());
		String updateDateTime = getJobDateTime(job.getUpdateDateTime());
		String updateUser = nvl(job.getUpdateUser());
		
		dpb0059Resp.setApptJobId(job.getApptJobId());
		dpb0059Resp.setRefItemNo(refItemNo);
		dpb0059Resp.setRefSubitemNo(refSubitemNo);
		dpb0059Resp.setStatus(status);
		dpb0059Resp.setStartDateTime(startDateTime);
		dpb0059Resp.setJobStep(jobStep);
		dpb0059Resp.setExecResult(execResult);
		dpb0059Resp.setCreateDateTime(createDateTime);
		dpb0059Resp.setCreateUser(createUser);
		dpb0059Resp.setUpdateDateTime(updateDateTime);
		dpb0059Resp.setUpdateUser(updateUser);
		
		return dpb0059Resp;
	}

	private String getRefItemNo(String refItemNo, String locale) {
		List<TsmpDpItems> items = getTsmpDpItemsCacheProxy().findByItemNoAndLocale(refItemNo, locale);
		if (items != null && !items.isEmpty()) {
			String itemName = items.get(0).getItemName();
			return refItemNo.concat(" - ").concat(itemName);
		}
		return new String();
	}

	private String getRefSubitemNo(String refItemNo, String refSubitemNo, String locale) {
		if (!StringUtils.isEmpty(refSubitemNo)) {
			TsmpDpItems items = getItemsById(refItemNo, refSubitemNo, false, locale);
			if (items != null) {
				return refSubitemNo.concat(" - ").concat(items.getSubitemName());
			}
		}
		return new String();
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw DgrRtnCode._1297.throwing();
		}
		return i;
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

	private String getJobDateTime(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分_2).orElse(new String());
	}

	private String nvl(Object input) {
		if (input == null) {
			return new String();
		}
		return input.toString();
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}



}
