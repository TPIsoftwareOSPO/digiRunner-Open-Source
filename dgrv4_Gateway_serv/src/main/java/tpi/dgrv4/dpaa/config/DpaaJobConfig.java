package tpi.dgrv4.dpaa.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.HttpType;
import tpi.dgrv4.dpaa.component.ApiHelper_TSMP;
import tpi.dgrv4.dpaa.component.TsmpInvokeHelper;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherJobHelper;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierCustom;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierLine;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierRoleEmail;
import tpi.dgrv4.dpaa.component.apptJob.ApiApplicationJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiOffJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiOnJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiOnUpdateJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledDisable;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledEnable;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledLaunch;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledRemoval;
import tpi.dgrv4.dpaa.component.apptJob.BroadcastJob;
import tpi.dgrv4.dpaa.component.apptJob.CallApiJob;
import tpi.dgrv4.dpaa.component.apptJob.CallTsmpApiJob;
import tpi.dgrv4.dpaa.component.apptJob.ClientRegJob;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertDetectorJobKeyword;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertDetectorJobSystemBasic;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertJob_Line;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertJob_RoleEmail;
import tpi.dgrv4.dpaa.component.apptJob.HandleReportDataJob;
import tpi.dgrv4.dpaa.component.apptJob.HousekeepingJob;
import tpi.dgrv4.dpaa.component.apptJob.NoticeExpCertJob;
import tpi.dgrv4.dpaa.component.apptJob.OakChkExpiJob;
import tpi.dgrv4.dpaa.component.apptJob.OpenApiKeyApplicaJob;
import tpi.dgrv4.dpaa.component.apptJob.OpenApiKeyRevokeJob;
import tpi.dgrv4.dpaa.component.apptJob.OpenApiKeyUpdateJob;
import tpi.dgrv4.dpaa.component.apptJob.SendMailApptJob;
import tpi.dgrv4.dpaa.component.apptJob.SyncTsmpdpapiToDpClientJob;
import tpi.dgrv4.dpaa.component.apptJob.SystemMonitorJob;
import tpi.dgrv4.dpaa.component.apptJob.TsmpInvokeJob;
import tpi.dgrv4.dpaa.component.cache.proxy.DgrWebsiteCacheProxy;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.dpaa.component.job.AA0001Job;
import tpi.dgrv4.dpaa.component.job.AA0004Job;
import tpi.dgrv4.dpaa.component.job.AA0201Job;
import tpi.dgrv4.dpaa.component.job.AA0231Job;
import tpi.dgrv4.dpaa.component.job.ComposerServiceJob;
import tpi.dgrv4.dpaa.component.job.DPB0002Job;
import tpi.dgrv4.dpaa.component.job.DPB0005Job;
import tpi.dgrv4.dpaa.component.job.DPB0006Job;
import tpi.dgrv4.dpaa.component.job.DPB0046Job;
import tpi.dgrv4.dpaa.component.job.DPB0065Job;
import tpi.dgrv4.dpaa.component.job.DPB0067Job;
import tpi.dgrv4.dpaa.component.job.DPB0071MailJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredOpenApiKeyJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredTempFileJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredTempFileTsmpDpFileJob;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.component.job.RefreshAuthCodeJob;
import tpi.dgrv4.dpaa.component.job.SaveEventJob;
import tpi.dgrv4.dpaa.component.job.SendMailJob;
import tpi.dgrv4.dpaa.component.job.SendOpenApiKeyExpiringMailJob;
import tpi.dgrv4.dpaa.component.job.SendOpenApiKeyMailJob;
import tpi.dgrv4.dpaa.component.job.SendReviewMailJob;
import tpi.dgrv4.dpaa.component.nodeTask.CleanAllCacheNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.CleanCacheByKeyNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.CleanCacheByTableNameNotifier;
import tpi.dgrv4.dpaa.component.req.DpReqServiceFactory;
import tpi.dgrv4.dpaa.component.rjob.ThinkpowerArticleJob;
import tpi.dgrv4.dpaa.es.DgrESService;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.HandleDashboardDataByYearService;
import tpi.dgrv4.dpaa.service.HandleDashboardDataService;
import tpi.dgrv4.dpaa.service.HandleDashboardLogDataService;
import tpi.dgrv4.dpaa.service.HandleESApiDataService;
import tpi.dgrv4.dpaa.service.HandleESExpiredDataService;
import tpi.dgrv4.dpaa.service.HandleReportDataByDayService;
import tpi.dgrv4.dpaa.service.HandleReportDataByHourService;
import tpi.dgrv4.dpaa.service.HandleReportDataByMinuteService;
import tpi.dgrv4.dpaa.service.HandleReportDataByMonthService;
import tpi.dgrv4.dpaa.service.HandleReportDataByYearService;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyExpiringMailService;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.dpaa.service.composer.ComposerService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.repository.DgrAcIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrAuditLogDDao;
import tpi.dgrv4.entity.repository.DgrAuditLogMDao;
import tpi.dgrv4.entity.repository.DgrImportClientRelatedTempDao;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpAuthCodeDao;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientLogDao;
import tpi.dgrv4.entity.repository.TsmpDpApiAuth2Dao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpCallapiDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpMailLogDao;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd1Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd3Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd4Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpEventsDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpNoticeLogDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.autoInitSQL.AutoInitSQL;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.gateway.component.check.TrafficCheck;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.component.job.appt.HttpUtilJob;
import tpi.dgrv4.gateway.config.BeanConfig;
import tpi.dgrv4.gateway.service.WebsiteService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Configuration
@EnableScheduling
public class DpaaJobConfig {
	
	private SendOpenApiKeyExpiringMailService sendOpenApiKeyExpiringMailService; // non-Singleton

	private final ApplicationContext applicationContext;
	private final BeanConfig beanConfig;
	private final TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private final TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao;
	private final TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private final TsmpApiDao tsmpApiDao;
	private final TsmpApiExtDao tsmpApiExtDao;
	private final FileHelper fileHelper;
	private final TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private final TsmpDpFileDao tsmpDpFileDao;
	private final SeqStoreService seqStoreService;
	private final MailHelper mailHelper;
	private final TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao;
	private final TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao;
	private final TsmpClientDao tsmpClientDao;
	private final TsmpGroupApiDao tsmpGroupApiDao;
	private final TsmpClientGroupDao tsmpClientGroupDao;
	private final TsmpGroupDao tsmpGroupDao;
	private final TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private final TsmpDpClientextDao tsmpDpClientextDao;
	private final DgrAuditLogService dgrAuditLogService;
	private final SendOpenApiKeyMailService sendOpenApiKeyMailService;
	private final TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao;
	private final TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao;
	private final TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private final TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	private final TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private final TsmpEventsDao tsmpEventsDao;
	private final TsmpClientLogDao tsmpClientLogDao;
	private final TsmpDpApptJobDao tsmpDpApptJobDao;
	private final DpReqServiceFactory dpReqServiceFactory;
	private final TsmpNoticeLogDao tsmpNoticeLogDao;
	private final TsmpSettingDao tsmpSettingDao;
	private final TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private final DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao;
	private final DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao;
	private final TrafficCheck trafficCheck;
	private final WebsiteService websiteService;
	private final DgrWebsiteCacheProxy dgrWebsiteCacheProxy;
	private final TsmpSettingService tsmpSettingService;
	private final ObjectMapper objectMapper;
	private final TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
	private final TsmpClientCertDao tsmpClientCertDao;
	private final TsmpClientCert2Dao tsmpClientCert2Dao;
	private final TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	private final PrepareMailService prepareMailService;
	private final TsmpInvokeHelper tsmpInvokeHelper;
	private final ApiHelper_TSMP apiHelper;
	private final TsmpDpCallapiDao tsmpDpCallapiDao;
	private final TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao;
	private final OauthClientDetailsDao oauthClientDetailsDao;
	private final TsmpDpNewsDao tsmpDpNewsDao;
	private final SendReviewMailService sendReviewMailService;
	private final TsmpDpMailLogDao tsmpDpMailLogDao;
	private final ComposerService composerService;
	private final TsmpUserDao tsmpUserDao;
	private final ServiceConfig serviceConfig;
	private final ApptJobDispatcher apptJobDispatcher;
	private final TsmpAuthCodeDao tsmpAuthCodeDao;
	private final TsmpFuncDao tsmpFuncDao;
	private final TsmpRoleFuncDao tsmpRoleFuncDao;
	private final AutoInitSQL autoInitSQL;
	private final TsmpTAEASKHelper tsmpTAEASKHelper;
	private final TsmpAlertDao tsmpAlertDao;
	private final CleanAllCacheNotifier cleanAllCacheNotifier;
	private final CleanCacheByKeyNotifier cleanCacheByKeyNotifier;
	private final CleanCacheByTableNameNotifier cleanCacheByTableNameNotifier;
	private final HandleReportDataByMinuteService handleReportDataByMinuteService;
	private final HandleReportDataByHourService handleReportDataByHourService;
	private final HandleReportDataByDayService handleReportDataByDayService;
	private final HandleReportDataByMonthService handleReportDataByMonthService;
	private final HandleReportDataByYearService handleReportDataByYearService;
	private final HandleDashboardLogDataService handleDashboardLogDataService;
	private final HandleDashboardDataService handleDashboardDataService;
	private final HandleDashboardDataByYearService handleDashboardDataByYearService;
	private final HandleESApiDataService handleESApiDataService;
	private final HandleESExpiredDataService handleESExpiredDataService;
	private final tpi.dgrv4.gateway.service.TsmpSettingService getwayTsmpSettingService;
    private final DgrAuditLogMDao dgrAuditLogMDao;
    private final DgrAuditLogDDao dgrAuditLogDDao;
    private final DpaaAlertNotifierRoleEmail dpaaAlertNotifierRoleEmail;
    private final DpaaAlertNotifierLine dpaaAlertNotifierLine;
    private final DpaaAlertNotifierCustom dpaaAlertNotifierCustom;
    private final DpaaAlertDispatcherJobHelper dpaaAlertDispatcherJobHelper;
    @Nullable
    private final DgrESService dgrESService;

	
	@Autowired
	private void setSendOpenApiKeyExpiringMailService() {
		this.sendOpenApiKeyExpiringMailService = applicationContext.getBean(SendOpenApiKeyExpiringMailService.class); // non-Singleton
	}

//	@Bean
//	public JobManager mainJobManager() {
//		return new JobManager();
//	}
//
//	@Bean
//	public DeferrableJobManager deferrableJobManager() {
//		return new DeferrableJobManager();
//	}
//
//	@Bean
//	public DeferrableJobManager refreshCacheJobManager() {
//		return new DeferrableJobManager();
//	}
//
//	@Bean
//	@Scope(value = "prototype")
//	public RefreshCacheJob refreshCacheJob(String cacheName, String key, Supplier<Object> supplier) {
//		return new RefreshCacheJob(cacheName, key, supplier);
//	}

	@Bean
	@Scope("prototype")
	public AA0001Job aa0001Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0001Job(auth, mailEvents, sendTime, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public AA0004Job aa0004Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0004Job(auth, mailEvents, sendTime, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public AA0201Job aa0201Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0201Job(auth, mailEvents, sendTime, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public AA0231Job aa0231Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0231Job(auth, mailEvents, sendTime, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public DPB0006Job dpb0006Job() {
		return new DPB0006Job(this.tsmpClientDao, this.tsmpGroupDao, this.tsmpClientGroupDao, this.tsmpGroupApiDao,
				this.tsmpDpClientextDao, this.oauthClientDetailsDao, this.tsmpDpFileDao, this.fileHelper,
				this.tsmpDpReqOrderd3Dao, this.tsmpDpItemsCacheProxy, this.dgrAuditLogService);
	}

	@Bean
	@Scope("prototype")
	public DPB0002Job dpb0002Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new DPB0002Job(auth, mailEvents, sendTime, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public DPB0005Job dpb0005Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new DPB0005Job(auth, mailEvents, sendTime, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public DPB0046Job dpb0046Job() {
		return new DPB0046Job(this.tsmpDpNewsDao, this.tsmpDpItemsCacheProxy);
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredTempFileJob deleteExpiredTempFileJob() {
		return new DeleteExpiredTempFileJob(this.fileHelper, this.tsmpSettingService);
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredTempFileTsmpDpFileJob deleteExpiredTempFileTsmpDpFileJob(boolean switchPatternFileName) {
		return new DeleteExpiredTempFileTsmpDpFileJob(switchPatternFileName, this.fileHelper, this.tsmpSettingService);
	}

	@Bean
	@Scope("prototype")
	public SendReviewMailJob sendReviewMailJob(TsmpAuthorization auth, Long reqOrdermId, String reqType,
			String sendTime, String reqOrderNo, String locale) {
		return new SendReviewMailJob(auth, reqOrdermId, reqType, sendTime, reqOrderNo, locale, this.sendReviewMailService,
				this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public SendOpenApiKeyMailJob sendOpenApiKeyMailJob(TsmpAuthorization auth, String sendTime, Long openApiKeyId,
			String openApiKeyType, String reqOrderNo) {
		return new SendOpenApiKeyMailJob(auth, sendTime, openApiKeyId, openApiKeyType, reqOrderNo,
				this.sendOpenApiKeyMailService, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public SendOpenApiKeyExpiringMailJob sendOpenApiKeyExpiringMailJob(TsmpAuthorization auth, String sendTime,
			Long openApiKeyId) {
		return new SendOpenApiKeyExpiringMailJob(auth, sendTime, openApiKeyId, this.applicationContext,
				this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public DPB0065Job dpb0065Job(String locale, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, SeqStoreDao seqStoreDao) {
		return new DPB0065Job(locale, tsmpDpItemsCacheProxy, seqStoreDao);
	}

	@Bean
	@Scope("prototype")
	public SaveEventJob saveEventJob(TsmpEvents tsmpEvents) {
		return new SaveEventJob(tsmpEvents, tsmpEventsDao, tsmpDpItemsCacheProxy, seqStoreService);
	}

	@Bean
	@Scope("prototype")
	public NoticeClearCacheEventsJob noticeClearCacheEventsJob(Integer action, String cacheName,
			List<String> tableNameList) {
		return new NoticeClearCacheEventsJob(action, cacheName, tableNameList, this.tsmpDpItemsCacheProxy,
				this.cleanAllCacheNotifier, this.cleanCacheByKeyNotifier, this.cleanCacheByTableNameNotifier);
	}

	@Bean
	@Scope("prototype")
	public RefreshAuthCodeJob refreshAuthCodeJob(Long expDay) {
		return new RefreshAuthCodeJob(expDay, this.tsmpAuthCodeDao);
	}

	@Bean
	@Scope("prototype")
	public ComposerServiceJob composerServiceJob(Integer act, List<String> apiUUIDs) {
		return new ComposerServiceJob(act, apiUUIDs, composerService);
	}

	/** 排程 Job */
	@Bean
	@Scope("prototype")
	public ApiOnJob apptJob_API_ON_OFF_API_ON(TsmpDpApptJob job) {
		return new ApiOnJob(job, this.tsmpDpReqOrdermDao, this.tsmpDpReqOrderd2dDao, this.tsmpDpReqOrderd2Dao,
				this.tsmpApiDao, this.tsmpApiExtDao, this.fileHelper, this.tsmpDpApiThemeDao, this.tsmpDpFileDao,
				this.seqStoreService, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public ApiOffJob apptJob_API_ON_OFF_API_OFF(TsmpDpApptJob job) {
		return new ApiOffJob(job, this.tsmpDpReqOrdermDao, this.tsmpDpReqOrderd2Dao, this.tsmpApiDao,
				this.tsmpApiExtDao, this.tsmpDpFileDao, this.tsmpDpApiThemeDao, this.fileHelper, this.seqStoreService,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public ApiOnUpdateJob apptJob_API_ON_OFF_API_ON_UPDATE(TsmpDpApptJob job) {
		return new ApiOnUpdateJob(job, this.tsmpDpReqOrdermDao, this.tsmpDpReqOrderd2dDao, this.tsmpApiDao,
				this.tsmpApiExtDao, this.tsmpDpReqOrderd2Dao, this.tsmpDpApiThemeDao, this.tsmpDpFileDao,
				this.fileHelper, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public CallTsmpApiJob apptJob_A_SCHEDULE_CALL_API1(TsmpDpApptJob job) {
		return new CallTsmpApiJob(job, this.apiHelper, this.tsmpDpCallapiDao, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public CallApiJob apptJob_A_SCHEDULE_CALL_API2(TsmpDpApptJob job) {
		return new CallApiJob(job, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public SendMailApptJob apptJob_SEND_MAIL(TsmpDpApptJob job) {
		return new SendMailApptJob(job, this.mailHelper, this.fileHelper, this.tsmpDpFileDao, this.apptJobDispatcher,
				this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredMailJob deleteExpiredMailJob() {
		return new DeleteExpiredMailJob(this.tsmpDpMailLogDao, this.tsmpDpItemsCacheProxy);
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredOpenApiKeyJob deleteExpiredOpenApiKeyJob() {
		return new DeleteExpiredOpenApiKeyJob(this.tsmpOpenApiKeyDao, this.tsmpOpenApiKeyMapDao, this.tsmpDpItemsCacheProxy);
	}

	@Bean
	@Scope("prototype")
	public ApiApplicationJob apptJob_API_APPLICATION(TsmpDpApptJob job) {
		return new ApiApplicationJob(job, this.tsmpDpReqOrdermDao, this.tsmpDpReqOrderd1Dao, this.tsmpDpApiAuth2Dao,
				this.tsmpClientDao, this.tsmpGroupApiDao, this.tsmpApiDao, tsmpClientGroupDao, this.tsmpGroupDao,
				this.seqStoreService, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public ClientRegJob apptJob_CLIENT_REG(TsmpDpApptJob job) {
		return new ClientRegJob(job, this.tsmpDpReqOrdermDao, this.tsmpDpReqOrderd3Dao, this.tsmpDpClientextDao,
				this.tsmpDpFileDao, this.fileHelper, this.dgrAuditLogService, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public OpenApiKeyApplicaJob apptJob_OPEN_API_KEY_OPEN_API_KEY_APPLICA(TsmpDpApptJob job) {
		return new OpenApiKeyApplicaJob(job, this.sendOpenApiKeyMailService, this.tsmpDpReqOrdermDao,
				this.tsmpDpReqOrderd5dDao, this.tsmpDpReqOrderd5Dao, this.tsmpOpenApiKeyDao, this.tsmpOpenApiKeyMapDao,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public OpenApiKeyUpdateJob apptJob_OPEN_API_KEY_OPEN_API_KEY_UPDATE(TsmpDpApptJob job) {
		return new OpenApiKeyUpdateJob(job, this.sendOpenApiKeyMailService, this.tsmpDpReqOrdermDao,
				this.tsmpDpReqOrderd5Dao, this.tsmpDpReqOrderd5dDao, this.tsmpOpenApiKeyDao, this.tsmpOpenApiKeyMapDao,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public OpenApiKeyRevokeJob apptJob_OPEN_API_KEY_OPEN_API_KEY_REVOKE(TsmpDpApptJob job) {
		return new OpenApiKeyRevokeJob(job, this.sendOpenApiKeyMailService, this.tsmpDpReqOrdermDao,
				this.tsmpDpReqOrderd5Dao, this.tsmpOpenApiKeyDao, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public OakChkExpiJob apptJob_OAK_CHK_EXPI(TsmpDpApptJob job) {
		return new OakChkExpiJob(job, this.tsmpOpenApiKeyDao, this.tsmpDpItemsCacheProxy, this.apptJobDispatcher,
				this.tsmpDpApptJobDao, this.applicationContext);
	}

	@Bean
	@Scope("prototype")
	public SyncTsmpdpapiToDpClientJob apptJob_SYNC_DATA1(TsmpDpApptJob job) {
		return new SyncTsmpdpapiToDpClientJob(job, this.apptJobDispatcher, this.tsmpDpApptJobDao, this.tsmpFuncDao,
				this.tsmpRoleFuncDao, this.tsmpClientDao, this.oauthClientDetailsDao, this.tsmpSettingDao,
				this.autoInitSQL, this.dgrAuditLogService, this.tsmpTAEASKHelper);
	}

	@Bean
	@Scope("prototype")
	public HandleReportDataJob apptJob_REPORT_BATCH(TsmpDpApptJob job) {
		return new HandleReportDataJob(job, apptJobDispatcher, tsmpDpApptJobDao, 
				handleReportDataByMinuteService, handleReportDataByHourService, 
				handleReportDataByDayService, handleReportDataByMonthService, 
				handleReportDataByYearService, handleDashboardLogDataService, 
				handleDashboardDataService, handleDashboardDataByYearService, this.getwayTsmpSettingService , 
				handleESApiDataService, handleESExpiredDataService);
	}

	@Bean
	@Scope("prototype")
	public HousekeepingJob apptJob_HOUSEKEEPING_BATCH(TsmpDpApptJob job) {
		return new HousekeepingJob(job, this.tsmpEventsDao, this.tsmpClientLogDao, this.tsmpDpApptJobDao,
				this.tsmpDpItemsCacheProxy, this.dpReqServiceFactory, this.tsmpDpReqOrdermDao, this.tsmpNoticeLogDao,
				this.tsmpDpFileDao, this.tsmpSettingDao, this.tsmpClientDao, this.tsmpTokenHistoryDao,
				this.dgrAcIdpAuthCodeDao, this.dgrImportClientRelatedTempDao, this.trafficCheck, this.websiteService,
				this.dgrWebsiteCacheProxy, this.tsmpSettingService, this.apptJobDispatcher,this.dgrAuditLogMDao,this.dgrAuditLogDDao);
	}
	
	@Bean
	@Scope("prototype")
	public HttpUtilJob apptJob_HTTP_UTIL_CALL_C_APIKEY(TsmpDpApptJob job) {
		return new HttpUtilJob(job, HttpType.C_APIKEY, this.fileHelper, this.objectMapper,
				this.tsmpCoreTokenHelperCacheProxy, this.tsmpSettingService,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public HttpUtilJob apptJob_HTTP_UTIL_CALL_NO_AUTH(TsmpDpApptJob job) {
		return new HttpUtilJob(job, HttpType.NO_AUTH, this.fileHelper, this.objectMapper,
				this.tsmpCoreTokenHelperCacheProxy, this.tsmpSettingService,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public HttpUtilJob apptJob_HTTP_UTIL_CALL_BASIC(TsmpDpApptJob job) {
		return new HttpUtilJob(job, HttpType.BASIC, this.fileHelper, this.objectMapper,
				this.tsmpCoreTokenHelperCacheProxy, this.tsmpSettingService,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledRemoval apptJob_API_SCHEDULED_API_REMOVAL(TsmpDpApptJob job) {
		return new ApiScheduledRemoval(job, this.apptJobDispatcher, this.tsmpDpApptJobDao, this.tsmpApiDao);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledLaunch apptJob_API_SCHEDULED_API_LAUNCH(TsmpDpApptJob job) {
		return new ApiScheduledLaunch(job, this.apptJobDispatcher, this.tsmpDpApptJobDao, this.tsmpApiDao);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledEnable apptJob_API_SCHEDULED_API_ENABLE(TsmpDpApptJob job) {
		return new ApiScheduledEnable(job, this.apptJobDispatcher, this.tsmpDpApptJobDao, this.tsmpApiDao);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledDisable apptJob_API_SCHEDULED_API_DISABLE(TsmpDpApptJob job) {
		return new ApiScheduledDisable(job, this.apptJobDispatcher, this.tsmpDpApptJobDao, this.tsmpApiDao);
	}

	@Bean
	@Scope("prototype")
	public NoticeExpCertJob apptJob_NOTICE_EXP_CERT_JWE(TsmpDpApptJob job) {
		return new NoticeExpCertJob(job, this.tsmpClientDao, this.tsmpDpClientextDao, this.tsmpClientCertDao,
				this.tsmpClientCert2Dao, this.tsmpNoticeLogDao, this.tsmpDpMailTpltCacheProxy, this.prepareMailService,
				this.mailHelper, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public NoticeExpCertJob apptJob_NOTICE_EXP_CERT_TLS(TsmpDpApptJob job) {
		return new NoticeExpCertJob(job, this.tsmpClientDao, this.tsmpDpClientextDao, this.tsmpClientCertDao,
				this.tsmpClientCert2Dao, this.tsmpNoticeLogDao, this.tsmpDpMailTpltCacheProxy, this.prepareMailService,
				this.mailHelper, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public SystemMonitorJob apptJob_RUNLOOP_SYS_MONITOR(TsmpDpApptJob job) {
		return new SystemMonitorJob(job, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public BroadcastJob apptJob_BROADCAST_RESTART_DGR_MODULE(TsmpDpApptJob job) {
		return new BroadcastJob(job, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	/*
	 * v4 不須此排程，但因 BcryptParams 專利關係，TSMP_DP_ITEMS 的項目不可刪除，否則會造成 Index 亂掉
	 * 
	 * @Bean
	 * 
	 * @Scope("prototype") public RestartDgrModuleJob
	 * apptJob_RESTART_DGR_MODULE(TsmpDpApptJob job) { return new
	 * RestartDgrModuleJob(job); }
	 */

	/**
	 * 此為開發範例，非正式簽核單排程
	 * 
	 * @param job
	 * @return
	 */
	@Bean
	@Scope("prototype")
	public ThinkpowerArticleJob apptJob_THINKPOWER_ARTICLE(TsmpDpApptJob job) {
		return new ThinkpowerArticleJob(job, this.tsmpDpReqOrdermDao, this.tsmpDpReqOrderd4Dao, this.apptJobDispatcher,
				this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public TsmpInvokeJob apptJob_TSMP_INVOKE(TsmpDpApptJob job) {
		return new TsmpInvokeJob(job, this.objectMapper, this.tsmpInvokeHelper, this.fileHelper, this.tsmpDpFileDao,
				this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public DPB0067Job dpb0067Job(String locale) {
		return new DPB0067Job(locale, this.tsmpDpItemsCacheProxy, this.tsmpDpReqOrdermDao, this.dpReqServiceFactory);
	}

	@Bean
	@Scope("prototype")
	public DPB0071MailJob dpb0071MailJob(TsmpAuthorization auth, List<TsmpMailEvent> tsmpMailEvent, String sendTime,
			Long reqOrdermId) {
		return new DPB0071MailJob(auth, tsmpMailEvent, sendTime, reqOrdermId, this.prepareMailService);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertJob_RoleEmail apptJob_DPAA_ALERT_ROLE_EMAIL(TsmpDpApptJob job) {
		return new DpaaAlertJob_RoleEmail(job, this.tsmpUserDao, this.prepareMailService, this.serviceConfig,
				this.objectMapper, this.tsmpSettingService, this.apptJobDispatcher, this.tsmpDpApptJobDao);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertJob_Line apptJob_DPAA_ALERT_LINE(TsmpDpApptJob job) {
		return new DpaaAlertJob_Line(job, this.apptJobDispatcher, this.tsmpDpApptJobDao, this.tsmpAlertDao,
				this.tsmpSettingService, this.objectMapper);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertDetectorJobKeyword apptJob_RUNLOOP_ALERT_KEYWORD(TsmpDpApptJob job) throws Exception {
		ObjectMapper om = beanConfig.objectMapper();
		return new DpaaAlertDetectorJobKeyword(job, om, this.apptJobDispatcher, this.tsmpDpApptJobDao, dpaaAlertNotifierRoleEmail, dpaaAlertNotifierLine, dpaaAlertNotifierCustom,
				dpaaAlertDispatcherJobHelper, tsmpAlertDao, dgrESService, tsmpSettingService);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertDetectorJobSystemBasic apptJob_RUNLOOP_ALERT_SYSTEM_BASIC(TsmpDpApptJob job) throws Exception {
		ObjectMapper om = beanConfig.objectMapper();
		return new DpaaAlertDetectorJobSystemBasic(job, om, this.apptJobDispatcher, this.tsmpDpApptJobDao, dpaaAlertNotifierRoleEmail, dpaaAlertNotifierLine, dpaaAlertNotifierCustom,
				dpaaAlertDispatcherJobHelper, tsmpAlertDao);
	}

	@Bean
	@Scope("prototype")
	public SendMailJob SendMailJob(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime,
			String identif) {
		return new SendMailJob(auth, mailEvents, sendTime, identif, this.prepareMailService);
	}

}
