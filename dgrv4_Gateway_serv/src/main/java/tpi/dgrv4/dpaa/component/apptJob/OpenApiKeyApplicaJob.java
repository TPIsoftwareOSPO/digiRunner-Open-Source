package tpi.dgrv4.dpaa.component.apptJob;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.OpenApiKeyUtil;
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
 * <b> 排程工作  </b><br>
 * 簽核類型: Open API Key 管理 - Open API Key 申請 <br>
 * 
 * @author Mini
 *
 */
@SuppressWarnings("serial")
public class OpenApiKeyApplicaJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private SendOpenApiKeyMailService sendOpenApiKeyMailService;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao;
	private TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;

	@Autowired
	public OpenApiKeyApplicaJob(TsmpDpApptJob tsmpDpApptJob, SendOpenApiKeyMailService sendOpenApiKeyMailService,
			TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao,
			TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.sendOpenApiKeyMailService = sendOpenApiKeyMailService;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd5dDao = tsmpDpReqOrderd5dDao;
		this.tsmpDpReqOrderd5Dao = tsmpDpReqOrderd5Dao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
	}

	@Override
	public String runApptJob() throws Exception {
		String app_reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(app_reqOrderNo)) {
			throw new Exception("未輸入單號");
		}

		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(app_reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + app_reqOrderNo);
		}

		Long app_reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd5> d5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(app_reqOrdermId);
		if (d5List == null || d5List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}

		step("PREP_OAK_APPLICA");// 準備建立 Open API Key

		return doOpenApiKeyApplica(m, d5List);
	}

	public String doOpenApiKeyApplica(TsmpDpReqOrderm m, List<TsmpDpReqOrderd5> d5List) {
		int successCnt = 0;
		Long reqOrdermId = m.getReqOrdermId();
		String reqOrderNo = m.getReqOrderNo();
		String userName = m.getCreateUser();

		TsmpDpReqOrderd5 d5 = null;
		for (int i = 0; i < d5List.size(); i++) {
			d5 = d5List.get(i);
			Long d5Id = d5.getReqOrderd5Id();
			
			try {
				// 更新 Open Api Key 檔
				TsmpOpenApiKey oak = insertOpenApiKey(d5, userName);
				Long openApiKeyId = oak.getOpenApiKeyId();
				
				// 更新 Open Api Key 和 API 對應檔
				insertOpenApiKeyMap(d5Id, openApiKeyId, userName);
				
				// 寄發申請成功Mail通知			
				sendSuccessEmail(reqOrdermId, openApiKeyId, TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_APPLICA.value(), reqOrderNo);

				// in-memory, 用列舉的值傳入值
				TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

				successCnt++;
			} catch (Exception e) {
				logger.debug(StackTraceUtil.logStackTrace(e));
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
	 * 針對選定的 Client ID 產生 Open API Key & Secret Key 並寫入 TSMP_OPEN_APIKEY ,
	 * 狀態註記為'1'
	 * 
	 * @param d5
	 * @param username
	 */
	private TsmpOpenApiKey insertOpenApiKey(TsmpDpReqOrderd5 d5, String username) {
		String openApiKey = OpenApiKeyUtil.createOpenApiKey();
		String secretKey = OpenApiKeyUtil.createSecretKey();

		TsmpOpenApiKey oak = new TsmpOpenApiKey();
		oak.setClientId(d5.getClientId());
		oak.setOpenApiKey(openApiKey);
		oak.setSecretKey(secretKey);
		oak.setOpenApiKeyAlias(d5.getOpenApiKeyAlias());
		oak.setTimesQuota(d5.getTimesThreshold());
		oak.setTimesThreshold(d5.getTimesThreshold());
		oak.setExpiredAt(d5.getExpiredAt());
		oak.setOpenApiKeyStatus(TsmpDpDataStatus.ON.value());// 1：啟用

		oak.setCreateDateTime(DateTimeUtil.now());
		oak.setCreateUser(username);//資料初始建立的人, 日期時間, 前台使用 clientName(UK), 後台使用 UserName(UK)
		oak = getTsmpOpenApiKeyDao().saveAndFlush(oak);
		
		return oak;
	}

	/**
	 * 將挑選的 API UUID 寫入 TSMP_OPEN_APIKEY_MAP (資料從申請單搬移到 Open API Key 授權表)
	 * 
	 * @param username
	 */
	private void insertOpenApiKeyMap(Long d5Id, Long openApiKeyId, String username) {
		List<TsmpDpReqOrderd5d> d5dList = getTsmpDpReqOrderd5dDao().findByRefReqOrderd5Id(d5Id);
		for (TsmpDpReqOrderd5d d5d : d5dList) {
			String apiUid = d5d.getRefApiUid();
			
			TsmpOpenApiKeyMap opaMap = new TsmpOpenApiKeyMap();
			opaMap.setRefOpenApiKeyId(openApiKeyId);
			opaMap.setRefApiUid(apiUid);
			opaMap.setCreateDateTime(DateTimeUtil.now());
			opaMap.setCreateUser(username);// 資料初始建立的人, 日期時間, 前台使用 clientName(UK), 後台使用 UserName(UK)
			opaMap = getTsmpOpenApiKeyMapDao().saveAndFlush(opaMap);
		}
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrderd5Dao getTsmpDpReqOrderd5Dao() {
		return this.tsmpDpReqOrderd5Dao;
	}
	
	protected SendOpenApiKeyMailService getSendOpenApiKeyMailService() {
		return this.sendOpenApiKeyMailService;
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
