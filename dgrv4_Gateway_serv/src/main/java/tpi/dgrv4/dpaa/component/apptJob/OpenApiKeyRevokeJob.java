package tpi.dgrv4.dpaa.component.apptJob;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyMailService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * <b> 排程工作 </b><br>
 * 簽核類型: Open API Key 管理 - Open API Key 撤銷 <br>
 * 
 * @author Mini
 *
 */
@SuppressWarnings("serial")
public class OpenApiKeyRevokeJob extends ApptJob {

	private SendOpenApiKeyMailService sendOpenApiKeyMailService;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;

	@Autowired
	public OpenApiKeyRevokeJob(TsmpDpApptJob tsmpDpApptJob, SendOpenApiKeyMailService sendOpenApiKeyMailService,
			TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao,
			TsmpOpenApiKeyDao tsmpOpenApiKeyDao, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher,  tsmpDpApptJobDao);
		this.sendOpenApiKeyMailService = sendOpenApiKeyMailService;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd5Dao = tsmpDpReqOrderd5Dao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
	}

	@Override
	public String runApptJob() throws Exception {
		String revoke_reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(revoke_reqOrderNo)) {
			throw new Exception("未輸入單號");
		}

		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(revoke_reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + revoke_reqOrderNo);
		}

		Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd5> revoke_d5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(reqOrdermId);
		if (revoke_d5List == null || revoke_d5List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}

		step("PREP_OAK_REVOKE");// 準備撤銷 Open API Key

		return doOpenApiKeyRevoke(m, revoke_d5List);
	}

	public String doOpenApiKeyRevoke(TsmpDpReqOrderm m, List<TsmpDpReqOrderd5> d5List) throws Exception {
		int successCnt = 0;
		Long reqOrdermId = m.getReqOrdermId();
		String reqOrderNo = m.getReqOrderNo();
		String userName = m.getCreateUser();

		TsmpDpReqOrderd5 d5 = null;
		for (int i = 0; i < d5List.size(); i++) {
			d5 = d5List.get(i);
			Long openApiKeyId = d5.getRefOpenApiKeyId();
			
			Optional<TsmpOpenApiKey> opt_oak = getTsmpOpenApiKeyDao().findById(openApiKeyId);
			if(!opt_oak.isPresent()) {
				throw new Exception("查無 Open Api Key 相關資料");
			}
			
			TsmpOpenApiKey oak = opt_oak.get();			
			try {
				// 更新 Open Api Key 檔
				updateOpenApiKey(oak, userName);
				
				// TSMP_OPEN_APIKEY_MAP, API不變更
				
				// 寄發撤銷成功Mail通知			
				sendSuccessEmail(reqOrdermId, openApiKeyId, TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_REVOKE.value(), reqOrderNo);

				// in-memory, 用列舉的值傳入值
				TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

				successCnt++;
			} catch (Exception e) {
				TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			} finally {
				step((i + 1) + "/" + d5List.size());
			}
		}

		return successCnt + "/" + d5List.size();
	}

	public void sendSuccessEmail(Long reqOrdermId, Long openApiKeyId, String openApiKeyType, String reqOrderNo) {
		getSendOpenApiKeyMailService().sendEmail(reqOrdermId, openApiKeyId, openApiKeyType, reqOrderNo);
	}
	
	/**
	 * 撤銷 Open API Key, 狀態註記為'0' 停用, 並寫入撤銷時間
	 */
	private TsmpOpenApiKey updateOpenApiKey(TsmpOpenApiKey oak, String username) {
		oak.setOpenApiKeyStatus(TsmpDpDataStatus.OFF.value());// 0：停用
		oak.setRevokedAt(DateTimeUtil.now().getTime());// 撤銷時間
		
		oak.setUpdateDateTime(DateTimeUtil.now());
		oak.setUpdateUser(username);// 資料初始建立的人, 日期時間, 前台使用 clientName(UK), 後台使用 UserName(UK)
		oak = getTsmpOpenApiKeyDao().saveAndFlush(oak);
		
		return oak;
	}
	
	protected SendOpenApiKeyMailService getSendOpenApiKeyMailService() {
		return this.sendOpenApiKeyMailService;
	}
	
	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrderd5Dao getTsmpDpReqOrderd5Dao() {
		return this.tsmpDpReqOrderd5Dao;
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
}
