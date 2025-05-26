package tpi.dgrv4.dpaa.component.rjob;

import java.util.List;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd4;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd4Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * <b>排程工作</b><br>
 * 簽核類型: 昕力大學文章<br>
 * 工作說明: 最後一關審核同意時, 要發送apptJob更新tsmp_dp_req_orderd4.is_publish = '1'
 *
 */

@SuppressWarnings("serial")
public class ThinkpowerArticleJob extends ApptJob {

	private static TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao;

	public ThinkpowerArticleJob(TsmpDpApptJob tsmpDpApptJob, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, logger, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd4Dao = tsmpDpReqOrderd4Dao;
	}

	@Override
	public String runApptJob() throws Exception {
		// 取得申請單編號
		String reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(reqOrderNo)) throw new Exception("未輸入單號");
		
		// 從申請單編號關聯出申請單主檔
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(reqOrderNo);
		if (m == null) throw new Exception("查無工作單: " + reqOrderNo);
		
		// 從主檔關聯出明細檔
		Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd4> d4List = getTsmpDpReqOrderd4Dao().findByRefReqOrdermId(reqOrdermId);
		if (d4List == null || d4List.isEmpty()) throw new Exception("申請單明細空白");
		
		int success = 0;
		TsmpDpReqOrderd4 d4 = null;
		for(int i = 0; i < d4List.size(); i++) {
			d4 = d4List.get(i);
			try {
				d4.setIsPublish(1);	// 更新文章狀態為"公開"
				d4.setUpdateDateTime(DateTimeUtil.now());
				d4.setUpdateUser("SYS");
				d4 = getTsmpDpReqOrderd4Dao().save(d4);
				success++;
			} catch (Exception e) {
				logger.debug("" + e);
			}
		}
		return success + "/" + d4List.size();
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrderd4Dao getTsmpDpReqOrderd4Dao() {
		return this.tsmpDpReqOrderd4Dao;
	}

}
