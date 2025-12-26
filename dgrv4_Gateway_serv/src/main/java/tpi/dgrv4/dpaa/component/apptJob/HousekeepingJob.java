package tpi.dgrv4.dpaa.component.apptJob;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.cache.proxy.DgrWebsiteCacheProxy;
import tpi.dgrv4.dpaa.component.req.DpReqServiceFactory;
import tpi.dgrv4.dpaa.component.req.DpReqServiceIfs;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpClientLog;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.entity.jpql.TsmpNoticeLog;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.check.TrafficCheck;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

import tpi.dgrv4.gateway.service.WebsiteService;

/**
 * housekeeping
 */
@SuppressWarnings("serial")
@Transactional
public class HousekeepingJob extends ApptJob {

    private TPILogger logger = TPILogger.tl;

    private TsmpEventsDao tsmpEventsDao;
    private TsmpClientLogDao tsmpClientLogDao;
    private TsmpDpApptJobDao tsmpDpApptJobDao;
    private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
    private DpReqServiceFactory dpReqServiceFactory;
    private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
    private TsmpNoticeLogDao tsmpNoticeLogDao;
    private TsmpDpFileDao tsmpDpFileDao;
    private TsmpSettingDao tsmpSettingDao;
    private TsmpClientDao tsmpClientDao;
    private TsmpTokenHistoryDao tsmpTokenHistoryDao;
    private DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao;
    private DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao;
    private TrafficCheck trafficCheck;
    private WebsiteService websiteService;
    private DgrWebsiteCacheProxy dgrWebsiteCacheProxy;
    private TsmpSettingService tsmpSettingService;
    private DgrAuditLogMDao dgrAuditLogMDao;
    private DgrAuditLogDDao dgrAuditLogDDao;
    private DgrWebhookNotifyLogDao dgrWebhookNotifyLogDao;

    @Autowired
	public HousekeepingJob(TsmpDpApptJob tsmpDpApptJob, TsmpEventsDao tsmpEventsDao, TsmpClientLogDao tsmpClientLogDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			DpReqServiceFactory dpReqServiceFactory, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpNoticeLogDao tsmpNoticeLogDao, TsmpDpFileDao tsmpDpFileDao, TsmpSettingDao tsmpSettingDao,
			TsmpClientDao tsmpClientDao, TsmpTokenHistoryDao tsmpTokenHistoryDao,
			DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao, DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao,
			TrafficCheck trafficCheck, WebsiteService websiteService, DgrWebsiteCacheProxy dgrWebsiteCacheProxy,
			TsmpSettingService tsmpSettingService, ApptJobDispatcher apptJobDispatcher,DgrAuditLogMDao dgrAuditLogMDao ,
                           DgrAuditLogDDao dgrAuditLogDDao, DgrWebhookNotifyLogDao dgrWebhookNotifyLogDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpEventsDao = tsmpEventsDao;
		this.tsmpClientLogDao = tsmpClientLogDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.dpReqServiceFactory = dpReqServiceFactory;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpNoticeLogDao = tsmpNoticeLogDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
		this.dgrAcIdpAuthCodeDao = dgrAcIdpAuthCodeDao;
		this.dgrImportClientRelatedTempDao = dgrImportClientRelatedTempDao;
		this.trafficCheck = trafficCheck;
		this.websiteService = websiteService;
		this.dgrWebsiteCacheProxy = dgrWebsiteCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrAuditLogMDao = dgrAuditLogMDao;
		this.dgrAuditLogDDao = dgrAuditLogDDao;
        this.dgrWebhookNotifyLogDao = dgrWebhookNotifyLogDao;
	}

    @Override
    public String runApptJob() throws Exception {

        /*
         * 第1部份:簽核作業 刪除主檔與審核狀態檔： TSMP_DP_REQ_ORDERM (申請單主檔) TSMP_DP_REQ_ORDERS
         * (申請單.審核狀態) 刪除審核歷程檔 TSMP_DP_CHK_LOG (申請單之審核歷程) 刪除申請單附件 TSMP_DP_FILE (上傳檔案)
         * 有四種申請單要刪除： 1.TSMP_DP_REQ_ORDERD1 (申請單-用戶申請API明細檔) 2.TSMP_DP_REQ_ORDERD2
         * (申請單-API上架/下架/異動) TSMP_DP_REQ_ORDERD2D (申請單-API上架/下架/異動 API mapping Theme)
         * TSMP_DP_FILE (上傳檔案) 3.TSMP_DP_REQ_ORDERD3 (申請單-用戶申請註冊) TSMP_CLIENT
         * (TSMP用戶端基本資料) TSMP_CLIENT_GROUP (TSMP用戶端與群組關係) TSMP_GROUP (TSMP用戶端群組)
         * TSMP_GROUP_API (TSMP群組與API關聯資料) TSMP_DP_CLIENTEXT (會員資料，額外客製資訊)
         * OAUTH_CLIENT_DETAILS (TSMP用戶端OAuth2驗證資料(Spring)) 4.TSMP_DP_REQ_ORDERD5
         * (申請單-Open API Key 申請/異動/撤銷) TSMP_DP_REQ_ORDERD5D (申請單-Open API Key 申請/異動/撤銷
         * mapping API)
         */
        deleteSignOff();

        /*
         * 第2部份: 事件檢視器TsmpEvents
         */
        deleteEvent();

        /*
         * 第3部份: 排程作業TSMP_DP_APPT_JOB、tsmp_dp_file
         */
        deleteApptJob();

        /*
         * 第4部份: 用戶端管理TsmpClientLog
         */
        deleteClientLog();

        /*
         * 第5部分: 通知歷程
         */
        deleteNoticeLog();

        /**
         *
         * 第6部分: 工程模式定時關閉
         *
         */
        disableEngineeringMode();

        /**
         * 第7部分: 重設Client API配額
         */
        refreshClientApiOuota(Calendar.getInstance());

        /**
         * 第8部分: TSMP_TOKEN_HISTORY
         */
        deleteTsmpTokenHistory();
        /**
         * 第9部分: DGR_AC_IDP_AUTH_CODE
         */

        deleteDgrAcIdpAuthCode();

        /**
         * 第10部分: 清除gateway和website的memory
         */
        deleteTrafficMemory();


        /** 第11部分: 清除刪除超過一個月的 批次處理暫存檔
         *
         */
        deleteApiBatchModifyTemp();

        /**
         * 第12部分: 允許異動系統預設資料定時關閉
         */
        disableUpdateDefaultDataMode();

        /**
         * 第13部分: 刪除暫存的client匯入資訊
         */
        deleteImportClientRelated();

        /**
         * 第14部分: Log留存 7 天未生效問題, 在 setting 中增加 log保留天數=LOG_RETENTION_DAYS
         */
        deleteLogFile();

        /**
         * 第15部分: 刪除過期安全稽核日誌
         */
        deleteExpiredAuditLog();

        /**
         * 第16部分: 刪除7天webhook log
         */
        deleteExpiredWebhookLog();

        return "SUCCESS";

    }

    public void deleteExpiredWebhookLog() {
        int expDay = 7;
        LocalDate ld = LocalDate.now().minusDays(expDay);
        Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        List<DgrWebhookNotifyLog> list = getDgrWebhookNotifyLogDao().findByCreateDateTimeBefore(expDate);
        int totalCount = list.size();
        if (totalCount == 0) {
            step("16. 0/0");
            return;
        }
        int currentIndex = 0;
        for(DgrWebhookNotifyLog log : list){
            currentIndex++;
            step(String.format("16. %d/%d", currentIndex, totalCount));
            getDgrWebhookNotifyLogDao().delete(log);
        }
    }

    public void deleteExpiredAuditLog() {

        int auditLogRetentionDays = getTsmpSettingService().getVal_AUDIT_LOG_RETENTION_DAYS();

        Date expDate = getExpDate("signOff", "gov_long");

        if (auditLogRetentionDays != 0) {
            expDate = new Date(System.currentTimeMillis() - auditLogRetentionDays * 24L * 60L * 60L * 1000L);
        }

        List<DgrAuditLogM> dgrAuditLogMList = dgrAuditLogMDao.findByCreateDateTimeBefore(expDate);
        List<DgrAuditLogD> dgrAuditLogDList = dgrAuditLogDDao.findByCreateDateTimeBefore(expDate);

        // 計算總數量
        int totalCount = dgrAuditLogMList.size() + dgrAuditLogDList.size();
        
        if (totalCount == 0) {
            step("15. 0/0");
            return;
        }

        int currentIndex = 0;

        // 先刪除詳細資料 (DgrAuditLogD)
        for (DgrAuditLogD auditLogD : dgrAuditLogDList) {
            currentIndex++;
            step(String.format("15. %d/%d", currentIndex, totalCount));
            dgrAuditLogDDao.delete(auditLogD);
        }

        // 再刪除主資料 (DgrAuditLogM)
        for (DgrAuditLogM auditLogM : dgrAuditLogMList) {
            currentIndex++;
            step(String.format("15. %d/%d", currentIndex, totalCount));
            dgrAuditLogMDao.delete(auditLogM);
        }
    }

    public void deleteLogFile() {
        step("14. 0/0");
        String logRetentionDays = getTsmpSettingService().getVal_LOG_RETENTION_DAYS();
        int retentionDays = 0;
        if (StringUtils.hasLength(logRetentionDays)) {
            retentionDays = Integer.parseInt(logRetentionDays);

        } else {
            retentionDays = 7;
        }
        List<File> list = new ArrayList<>();
        String logDir = getLogDir();
        File directory = new File(logDir);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isFileOlderThanDays(file, retentionDays)) {
                        list.add(file);
                    }
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {
           var  retVal = list.get(i).delete();
           if (!retVal)
               TPILogger.tl.error("deleteLogFile fail" + list.get(i).getName());
            step(String.format("14. %d/%d", (i + 1), list.size()));
        }

    }

    protected String getLogDir() {
        return "logs";
    }

    private static boolean isFileOlderThanDays(File file, int days) {
        try {
            Path filePath = Paths.get(file.getAbsolutePath());
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            Instant fileTime = attrs.lastModifiedTime().toInstant();
            LocalDateTime fileDateTime = LocalDateTime.ofInstant(fileTime, ZoneId.systemDefault());
            LocalDateTime cutoffDateTime = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            return fileDateTime.isBefore(cutoffDateTime);
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
        }
        return false;
    }


    public void deleteTrafficMemory() {
        try {
            // gateway
            step("10. 1/2");
            List<String> keyList = new ArrayList<>(getTrafficCheck().map.keySet());
            for (String key : keyList) {
                boolean isExist = getTsmpClientDao().existsById(key);
                if (!isExist) {
                    getTrafficCheck().map.remove(key);
                }
            }

            // website
            step("10. 2/2");
            keyList = new ArrayList<>(getWebsiteService().trifficMap.keySet());
            for (String key : keyList) {
                DgrWebsite vo = getDgrWebsiteCacheProxy().findFirstByWebsiteName(key);
                if (vo == null) {
                    getWebsiteService().trifficMap.remove(key);
                }
            }
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
        }
    }

    public void deleteDgrAcIdpAuthCode() {
        Date expDate = getExpDate("dgrAcIdpAuthCode", "short");
        List<DgrAcIdpAuthCode> list = getDgrAcIdpAuthCodeDao().findByExpireDateTimeBefore(expDate.getTime());

        if (CollectionUtils.isEmpty(list)) {
            step("9. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("9. %d/%d", (i + 1), list.size()));
            DgrAcIdpAuthCode vo = list.get(i);
            getDgrAcIdpAuthCodeDao().delete(vo);
        }

    }

    public void deleteTsmpTokenHistory() {
        Date expDate = getExpDate("tokenHistory", "short");
        List<TsmpTokenHistory> list = getTsmpTokenHistoryDao().findByReexpiredAtBefore(expDate);

        if (CollectionUtils.isEmpty(list)) {
            step("8. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("8. %d/%d", (i + 1), list.size()));
            TsmpTokenHistory vo = list.get(i);
            getTsmpTokenHistoryDao().delete(vo);
        }

    }

    private void doRefresh() {
        List<TsmpClient> list = getTsmpClientDao().findByApiUsedGreaterThan(0);// 查詢API使用量 > 0
        for (int i = 0; i < list.size(); i++) {
            step(String.format("7. %d/%d", (i + 1), list.size()));
            TsmpClient tsmpClient = list.get(i);
            tsmpClient.setApiUsed(0);
            tsmpClient.setUpdateTime(DateTimeUtil.now());
            tsmpClient.setUpdateUser("SYSTEM apiUsed refresh");
            getTsmpClientDao().save(tsmpClient);
        }
    }

    public void refreshClientApiOuota(Calendar now) {
        Optional<TsmpSetting> getRefreshFRQ = getTsmpSettingDao().findById("DGR_CLIENT_QUOTA_FRQ");
        if (getRefreshFRQ.isEmpty()) return;
        String frq = getRefreshFRQ.get().getValue();
        // SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, and SATURDAY.
        // Calendar星期排序 順序1-7
        if ("N".equalsIgnoreCase(frq)) {
            return;
        } else if ("D".equalsIgnoreCase(frq)) {// 每日
            this.logger.debug("1");
            doRefresh();
        } else if ("SD".equalsIgnoreCase(frq) && 1 == now.get(Calendar.DAY_OF_WEEK)) { // 每周日
            this.logger.debug("2");
            doRefresh();
        } else if ("MD".equalsIgnoreCase(frq) && 2 == now.get(Calendar.DAY_OF_WEEK)) { // 每周一
            this.logger.debug("3");
            doRefresh();
        } else if ("MT".equalsIgnoreCase(frq) && 1 == now.get(Calendar.DAY_OF_MONTH)) { // 每月一號
            this.logger.debug("4");
            doRefresh();
        } else {
            return;
        }
    }

    public void deleteSignOff() {
        Date expDate = getExpDate("signOff", "gov_long");
        List<TsmpDpReqOrderm> list = getTsmpDpReqOrdermDao().query_dpb0067_expired(expDate);
        if (CollectionUtils.isEmpty(list)) {
            step("1. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("1. %d/%d", (i + 1), list.size()));
            TsmpDpReqOrderm tsmpDpReqOrderm = list.get(i);
            try {
                DpReqServiceIfs dpReqService = getDpReqServiceFactory()
                        .getDpReqService(tsmpDpReqOrderm.getReqOrdermId(), () -> {
                            return TsmpDpAaRtnCode._1217.throwing();
                        });

                // 刪除草稿申請單相關資料
                dpReqService.deleteExpired(tsmpDpReqOrderm);

            } catch (TsmpDpAaException e) {
                if (!TsmpDpAaRtnCode._1287.getCode().equals(e.getError().getCode())) {
                    logger.debug(
                            String.format("過期申請單號: %d 刪除失敗: %s", tsmpDpReqOrderm.getReqOrdermId(), e.getMessage()));
                }
            }
        }
    }

    public void deleteEvent() {
        Date expDate = getExpDate("event", "mid");
        List<TsmpEvents> list = getTsmpEventsDao().findByKeepFlagAndCreateDateTimeBefore("N", expDate);
        if (CollectionUtils.isEmpty(list)) {
            step("2. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("2. %d/%d", (i + 1), list.size()));
            TsmpEvents vo = list.get(i);
            getTsmpEventsDao().delete(vo);
        }
    }

    public void deleteApptJob() {
        Date expDate = getExpDate("apptJob", "long");
        List<TsmpDpApptJob> list = getTsmpDpApptJobDao().findByUpdateDateTimeBefore(expDate);
        if (CollectionUtils.isEmpty(list)) {
            step("3. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("3. %d/%d", (i + 1), list.size()));
            TsmpDpApptJob vo = list.get(i);

            // 刪除DP_FILE 與 ApptJob關連。關連邏輯在DPB0061Service.getFileList
            List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(//
                    TsmpDpFileType.TSMP_DP_APPT_JOB.value(), vo.getApptJobId());
            if ("SEND_MAIL".equals(vo.getRefItemNo())) {
                String refFileCateCode = TsmpDpFileType.MAIL_CONTENT.value();
                String fileNameSuffix = String.valueOf(vo.getApptJobId()).concat(".mail");
                fileList.addAll(getTsmpDpFileDao().query_DPB0061Service_01(refFileCateCode, fileNameSuffix));
            }
            // 刪除過期的SCB_PASM檔案
            if ("HTTP_UTIL_CALL".equals(vo.getRefItemNo())) {
                String refFileCateCode = TsmpDpFileType.HTTP_UTIL_JOB_API.value();
                fileList.addAll(tsmpDpFileDao.findByRefFileCateCodeAndRefId(refFileCateCode, vo.getApptJobId()));
            }
            if (CollectionUtils.isEmpty(fileList) == false) {
                for (TsmpDpFile tsmpDpFile : fileList) {
                    getTsmpDpFileDao().delete(tsmpDpFile);
                }
            }

            getTsmpDpApptJobDao().delete(vo);
        }
    }

    public void deleteClientLog() {
        Date expDate = getExpDate("clientLog", "mid");
        List<TsmpClientLog> list = getTsmpClientLogDao().findByCreateTimeBefore(expDate);
        if (CollectionUtils.isEmpty(list)) {
            step("4. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("4. %d/%d", (i + 1), list.size()));
            TsmpClientLog vo = list.get(i);
            getTsmpClientLogDao().delete(vo);
        }
    }

    public void deleteNoticeLog() {
        Date expDate = getExpDate("event", "mid"); // 60天
        List<TsmpNoticeLog> list = getTsmpNoticeLogDao().findByLastNoticeDateTimeLessThan(expDate);
        if (CollectionUtils.isEmpty(list)) {
            step("5. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("5. %d/%d", (i + 1), list.size()));
            getTsmpNoticeLogDao().delete(list.get(i));
        }
    }

    private Date getExpDate(String execItem, String subitemNo) {
        TsmpDpItems itemVo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("HOUSEKEEPING", subitemNo,
                LocaleType.EN_US);
        int expDay = Integer.parseInt(itemVo.getParam1());
        LocalDate ld = LocalDate.now().minusDays(expDay);
        Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        Optional<String> opt = DateTimeUtil.dateTimeToString(expDate, DateTimeFormatEnum.西元年月日時分秒_2);
        if (opt.isPresent()) {
            this.logger.debug(
                    String.format("execItem = %s, subitemNo = %s, expDate = %s", execItem, subitemNo, opt.get()));
        } else {
            this.logger
                    .debug(String.format("execItem = %s, subitemNo = %s, expDate = %tc", execItem, subitemNo, expDate));
        }

        return expDate;
    }

    public void disableEngineeringMode() {
        Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingDao().findById("TSMP_ONLINE_CONSOLE");

        if (opt_tsmpSetting.isPresent()) {
            TsmpSetting tsmpSetting = opt_tsmpSetting.get();
            tsmpSetting.setValue("false");
            getTsmpSettingDao().saveAndFlush(tsmpSetting);
        }
        step("6. 1/1");
        Optional<TsmpSetting> show_all_properties = getTsmpSettingDao().findById("SHOW_ALL_PROPERTIES");

        if (show_all_properties.isPresent()) {
            TsmpSetting tsmpSetting = show_all_properties.get();
            tsmpSetting.setValue("false");
            getTsmpSettingDao().saveAndFlush(tsmpSetting);
        }

        step("6. 2/2");
    }

    public void deleteApiBatchModifyTemp() {
        Date expDate = getExpDate("apiBatchModifyTemp", "long"); // 90天
        // 刪除DP_FILE 與
        List<TsmpDpFile> list = getTsmpDpFileDao()
                .findByRefFileCateCodeAndCreateDateTimeLessThan(TsmpDpFileType.API_MODIFY_BATCH.value(), expDate);
        for (int i = 0; i < list.size(); i++) {
            step(String.format("11. %d/%d", (i + 1), list.size()));
            getTsmpDpFileDao().delete(list.get(i));
        }

    }

    public void disableUpdateDefaultDataMode() {
        Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingDao().findById("DEFAULT_DATA_CHANGE_ENABLED");

        if (opt_tsmpSetting.isPresent()) {
            TsmpSetting tsmpSetting = opt_tsmpSetting.get();
            tsmpSetting.setValue("false");
            getTsmpSettingDao().saveAndFlush(tsmpSetting);
        }

        step("12. 1/1");
    }

    public void deleteImportClientRelated() {
        Date expDate = getExpDate("ImportClientRelated", "short");
        List<DgrImportClientRelatedTemp> list = getDgrImportClientRelatedTempDao().findByCreateDateTimeBefore(expDate);
        if (CollectionUtils.isEmpty(list)) {
            step("13. 0/0");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            step(String.format("13. %d/%d", (i + 1), list.size()));
            DgrImportClientRelatedTemp vo = list.get(i);
            getDgrImportClientRelatedTempDao().delete(vo);
        }
    }

    protected DgrAcIdpAuthCodeDao getDgrAcIdpAuthCodeDao() {
        return dgrAcIdpAuthCodeDao;
    }

    protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
        return tsmpTokenHistoryDao;
    }

    protected TsmpClientDao getTsmpClientDao() {
        return tsmpClientDao;
    }

    protected TsmpEventsDao getTsmpEventsDao() {
        return tsmpEventsDao;
    }

    protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
        return tsmpDpItemsCacheProxy;
    }

    protected TsmpClientLogDao getTsmpClientLogDao() {
        return tsmpClientLogDao;
    }

    protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
        return tsmpDpApptJobDao;
    }

    protected DpReqServiceFactory getDpReqServiceFactory() {
        return this.dpReqServiceFactory;
    }

    protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
        return tsmpDpReqOrdermDao;
    }

    protected TsmpNoticeLogDao getTsmpNoticeLogDao() {
        return this.tsmpNoticeLogDao;
    }

    protected TsmpDpFileDao getTsmpDpFileDao() {
        return tsmpDpFileDao;
    }

    protected TsmpSettingDao getTsmpSettingDao() {
        return tsmpSettingDao;
    }

    protected DgrImportClientRelatedTempDao getDgrImportClientRelatedTempDao() {
        return dgrImportClientRelatedTempDao;
    }

    protected TrafficCheck getTrafficCheck() {
        return trafficCheck;
    }

    protected WebsiteService getWebsiteService() {
        return websiteService;
    }

    protected DgrWebsiteCacheProxy getDgrWebsiteCacheProxy() {
        return dgrWebsiteCacheProxy;
    }

    protected TsmpSettingService getTsmpSettingService() {
        return tsmpSettingService;
    }

    protected DgrWebhookNotifyLogDao getDgrWebhookNotifyLogDao(){
        return dgrWebhookNotifyLogDao;
    }
}