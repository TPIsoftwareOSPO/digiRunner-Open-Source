package tpi.dgrv4.dpaa.component.apptJob;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyMailService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * <b> 排程工作 </b><br>
 * 簽核類型: Open API Key 管理 - Open API Key 異動 <br>
 * 
 * @author Mini
 *
 */
@SuppressWarnings("serial")
public class OpenApiKeyUpdateJob extends ApptJob {

	private SendOpenApiKeyMailService sendOpenApiKeyMailService;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao;
	private TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;

	@Autowired
	public OpenApiKeyUpdateJob(TsmpDpApptJob tsmpDpApptJob, SendOpenApiKeyMailService sendOpenApiKeyMailService,
			TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao,
			TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.sendOpenApiKeyMailService = sendOpenApiKeyMailService;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd5Dao = tsmpDpReqOrderd5Dao;
		this.tsmpDpReqOrderd5dDao = tsmpDpReqOrderd5dDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
	}

	@Override
	public String runApptJob() throws Exception {
		String reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(reqOrderNo)) {
			throw new Exception("未輸入單號");
		}
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + reqOrderNo);
		}
		Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd5> d5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(reqOrdermId);
		if (d5List == null || d5List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}

		step("PREP_OAK_UPDATE");// 準備異動 Open API Key

		return doOpenApiKeyUpdate(m, d5List);
	}

	public String doOpenApiKeyUpdate(TsmpDpReqOrderm m, List<TsmpDpReqOrderd5> d5List) throws Exception {
		int successCnt = 0;
		Long reqOrdermId = m.getReqOrdermId();
		String reqOrderNo = m.getReqOrderNo();
		String userName = m.getCreateUser();

		TsmpDpReqOrderd5 d5 = null;
		for (int i = 0; i < d5List.size(); i++) {
			d5 = d5List.get(i);
			Long openApiKeyId = d5.getRefOpenApiKeyId();
			Long d5Id = d5.getReqOrderd5Id();
			
			Optional<TsmpOpenApiKey> opt_oak = getTsmpOpenApiKeyDao().findById(openApiKeyId);
			if(!opt_oak.isPresent()) {
				throw new Exception("查無 Open Api Key 相關資料");
			}
			
			try {
				TsmpOpenApiKey oak = opt_oak.get();		
				
				update(oak, d5, d5Id, userName);
				
				// 寄發異動成功Mail通知			
				sendSuccessEmail(reqOrdermId, openApiKeyId, TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_UPDATE.value(), reqOrderNo);

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
	
	@Transactional
	public void update(TsmpOpenApiKey oak, TsmpDpReqOrderd5 d5, Long d5Id, String userName) {
		// 更新 Open Api Key 檔
		updateOpenApiKey(d5, oak, userName);			
		// 更新 Open Api Key 和 API 對應檔 
		updateOpenApiKeyMap(d5Id, oak.getOpenApiKeyId(), userName);
	}
	
	public void sendSuccessEmail(Long reqOrdermId, Long openApiKeyId, String openApiKeyType, String reqOrderNo) {
		getSendOpenApiKeyMailService().sendEmail(reqOrdermId, openApiKeyId, openApiKeyType, reqOrderNo);
	}
	
	/**
	 * TSMP_OPEN_APIKEY, 更新資料, 狀態註記為'1' 
	 * 
	 * @param d5
	 * @param username
	 */
	private TsmpOpenApiKey updateOpenApiKey(TsmpDpReqOrderd5 d5, TsmpOpenApiKey oak, String username) {
		oak.setClientId(d5.getClientId());
//		oak.setOpenApiKey(d5.getOpenApiKey());
//		oak.setSecretKey(d5.getSecretKey());
		oak.setOpenApiKeyAlias(d5.getOpenApiKeyAlias());
		oak.setTimesQuota(d5.getTimesThreshold());
		oak.setTimesThreshold(d5.getTimesThreshold());
		oak.setExpiredAt(d5.getExpiredAt());
		oak.setOpenApiKeyStatus(TsmpDpDataStatus.ON.value());// 1：啟用

		oak.setUpdateDateTime(DateTimeUtil.now());
		oak.setUpdateUser(username);// 資料初始建立的人, 日期時間, 前台使用 clientName(UK), 後台使用 UserName(UK)
		oak = getTsmpOpenApiKeyDao().saveAndFlush(oak);
		
		return oak;
	}
 
	/**
	 * 將挑選的 API UUID 寫入 TSMP_OPEN_APIKEY_MAP
	 * 
	 * @param username
	 */
	private void updateOpenApiKeyMap(Long d5Id, Long openApiKeyId, String username) {
		// 刪除原有的對應關係
		getTsmpOpenApiKeyMapDao().deleteByRefOpenApiKeyId(openApiKeyId);
		
		//
		List<TsmpDpReqOrderd5d> d5dList = getTsmpDpReqOrderd5dDao().findByRefReqOrderd5Id(d5Id);
		if (d5dList == null || d5dList.isEmpty()) {
			// throw new Exception("申請單明細錯誤(d5d)");
			return;
		}
		
		d5dList.forEach((d5d) -> {
			String apiUid = d5d.getRefApiUid();
			
			TsmpOpenApiKeyMap opaMap = new TsmpOpenApiKeyMap();
			opaMap.setRefOpenApiKeyId(openApiKeyId);
			opaMap.setRefApiUid(apiUid);
			opaMap.setCreateDateTime(DateTimeUtil.now());
			opaMap.setCreateUser(username);// 資料初始建立的人, 日期時間, 前台使用 clientName(UK), 後台使用 UserName(UK)
			opaMap = getTsmpOpenApiKeyMapDao().saveAndFlush(opaMap);
		});
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
	
	protected TsmpDpReqOrderd5dDao getTsmpDpReqOrderd5dDao() {
		return this.tsmpDpReqOrderd5dDao;
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
	
	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return this.tsmpOpenApiKeyMapDao;
	}
}
