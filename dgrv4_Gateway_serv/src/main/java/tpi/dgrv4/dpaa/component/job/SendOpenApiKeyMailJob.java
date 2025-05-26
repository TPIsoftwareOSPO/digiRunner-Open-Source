package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@SuppressWarnings("serial")
public class SendOpenApiKeyMailJob extends Job {

	private TPILogger logger = TPILogger.tl;

	private SendOpenApiKeyMailService sendOpenApiKeyMailService;
	private PrepareMailService prepareMailService;

	private final TsmpAuthorization auth;
	private final String sendTime;
	private final Long openApiKeyId;
	private final String openApiKeyType;
	private final String reqOrderNo;

	@Autowired
	public SendOpenApiKeyMailJob(TsmpAuthorization auth, String sendTime, Long openApiKeyId, String openApiKeyType,
			String reqOrderNo, SendOpenApiKeyMailService sendOpenApiKeyMailService,
			PrepareMailService prepareMailService) {
		this.auth = auth;
		this.sendTime = sendTime;
		this.openApiKeyId = openApiKeyId;
		this.openApiKeyType = openApiKeyType;
		this.reqOrderNo = reqOrderNo;
		this.sendOpenApiKeyMailService = sendOpenApiKeyMailService;
		this.prepareMailService = prepareMailService;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin SendOpenApiKeyMailJob ---");
			if (this.auth == null) {
				// 未傳入授權資訊, 無法寄出通知信
				throw new Exception("Authorization information was not passed in, so the notification email cannot be sent.");
			}
			if (this.openApiKeyId == null) {
				// 未指定Open Api Key ID, 無法寄出通知信
				throw new Exception("Open Api Key ID not specified, unable to send notification email");
			}
			
			//identif 可寫入識別資料，ex: userName=mini 或 userName=mini,　reqOrdermId=17002	若有多個資料則以逗號和全型空白分隔
			String identif = "userName=" + auth.getUserName() 
						+ ",　mailType=OAK_PASS" 
						+ ",　reqOrderNo=" + reqOrderNo 
						+ ",　actType=" + openApiKeyType
						+ ",　openApiKeyId=" + openApiKeyId;
			
			// Open API Key 建立/異動/撤銷 成功的mail通知內容
			List<TsmpMailEvent> mailEvents = getSendOpenApiKeyMailService().getTsmpMailEvents(this.auth, this.openApiKeyId, this.openApiKeyType);
			if (mailEvents == null || mailEvents.isEmpty()) {
				// 取得信件參數失敗, 無法寄出通知信
				throw new Exception("Failed to obtain the letter parameters, unable to send the notification letter");
			}
			
			/* 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信 */
			
			//準備好資料,以寫入排程
			getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(), sendTime);
			
		} catch (Exception e) {
			logger.debug("" + e);
		} finally {
			this.logger.debug("--- Finish SendOpenApiKeyMailJob ---");
		}
	}

	protected SendOpenApiKeyMailService getSendOpenApiKeyMailService() {
		return sendOpenApiKeyMailService;
	}
	
	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}
}
