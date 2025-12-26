package tpi.dgrv4.entity.repository;

import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.ApptJobEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TsmpDpApptJobDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpApptJob> queryExecutableJobs(Date startDateTime) {
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" select A from TsmpDpApptJob A where 1 = 1");
		sql.append(" and A.status = :status");
		sql.append(" and A.startDateTime <= :startDateTime");
		sql.append(" order by A.startDateTime");

		params.put("status", TsmpDpApptJobStatus.WAIT.value());
		params.put("startDateTime", startDateTime);
		
		return doQuery(sql.toString(), params, TsmpDpApptJob.class);
	}
	
	public List<TsmpDpApptJob> queryByRefItemNoAndCreateDateTime(String refItemNo,String date){
		
		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(date, DateTimeFormatEnum.西元年月日_2);
		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(date, DateTimeFormatEnum.西元年月日_2);
		LocalDate ld_startDate = opt_startDate.orElseThrow(() -> new RuntimeException(date));
		LocalDate ld_endDate = opt_endDate.orElseThrow(() -> new RuntimeException(date)).plusDays(1L);
		Date startDate = Date.from(ld_startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Date endDate = Date.from(ld_endDate.atStartOfDay().plus(-1,ChronoUnit.SECONDS).atZone(ZoneId.systemDefault()).toInstant());
		
		
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT t ");
		sql.append(" FROM TsmpDpApptJob t ");
		sql.append(" WHERE 1 = 1 AND");
		sql.append(" refItemNo = :refItemNo AND ");
		sql.append(" status = :status AND ");
		sql.append(" createDateTime >= :startDate AND ");
		sql.append(" createDateTime <= :endDate ");
		sql.append(" ORDER BY apptJobId DESC ");	

		params.put("refItemNo", refItemNo);
		params.put("status", TsmpDpApptJobStatus.DONE.value());
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		return doQuery(sql.toString(), params, TsmpDpApptJob.class);
	}

	public List<TsmpDpApptJob> query_dpb0058Service(Date sdt, Date edt, String status, String[] words //
			, TsmpDpApptJob lastRecord, String locale, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" select J");
		sql.append(" from TsmpDpApptJob J");
		sql.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sql.append(" and ( 1 = 2");
			sql.append(" 	or (J.createDateTime < :lastCdt and J.apptJobId <> :lastId)");
			sql.append(" 	or (J.createDateTime = :lastCdt and J.apptJobId < :lastId)");
			sql.append(" )");
			params.put("lastCdt", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getApptJobId());
		}
		// 必要條件 (創建日期)
		sql.append(" and J.createDateTime between :sdt and :edt");
		// 可選條件 (執行狀態)。若為A：全部，不列入當條件。
		if (!StringUtils.isEmpty(status)&&!"A".equals(status)) {
			sql.append(" and J.status = :status");
			params.put("status", status);
		}
		// 關鍵字 (ID/狀態/識別資料, 大分類/子項目, 工作進度/執行結果值)
		if (words != null && words.length > 0) {
			sql.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				
				//Id 要轉型
				Long keywordId = null;
				try {
					keywordId = Long.valueOf(words[i]);
					sql.append(" 	or J.apptJobId = :keywordId" + i);//Long
					params.put(("keywordId" + i), keywordId);//Long
				}catch (Exception e) {
				}
 
				sql.append(" 	or UPPER(J.status) like :keyword" + i);
				sql.append(" 	or UPPER(J.identifData) like :keyword" + i);
				sql.append("	or UPPER(J.refItemNo) = :v_keyword" + i);
				sql.append("	or UPPER(J.refSubitemNo) = :v_keyword" + i);
				params.put(("keyword" + i), "%" + words[i].toUpperCase() + "%");
				params.put(("v_keyword" + i), words[i].toUpperCase());
			}
			
			// 大分類的中文
			sql.append(" 	or exists (");
			sql.append(" 		select 1");
			sql.append(" 		from TsmpDpItems I1");
			sql.append(" 		where I1.itemNo = :itemNo1");
			sql.append(" 		and I1.subitemNo = J.refItemNo");
			sql.append(" 		and I1.locale = :locale");
			sql.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sql.append(" 			or UPPER(I1.subitemName) like :keyword" + i);
			}
			sql.append(" 		)");
			sql.append(" 	)");
			params.put("itemNo1", "SCHED_CATE1");
			params.put("locale", locale);
			
			// 子項目的中文
			sql.append(" 	or exists (");
			sql.append(" 		select 1");
			sql.append(" 		from TsmpDpItems I2");
			sql.append(" 		where I2.itemNo = J.refItemNo");
			sql.append(" 		and I2.subitemNo = J.refSubitemNo");
			sql.append(" 		and I2.locale = :locale");
			sql.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sql.append(" 			or UPPER(I2.subitemName) like :keyword" + i);
			}
			sql.append(" 		)");
			sql.append(" 	)");
			
			// 工作進度的中文
			// 1.要找到轉換成 TSMP_DP_ITEMS 的 SUBITEM_NAME 的資料
			sql.append(" 	or exists (");
			sql.append(" 		select 1");
			sql.append(" 		from TsmpDpItems I3");
			sql.append(" 		where I3.itemNo = :itemNo3");
			sql.append(" 		and I3.subitemNo = J.jobStep");
			sql.append(" 		and I3.locale = :locale");
			sql.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sql.append(" 			or UPPER(I3.subitemName) like :keyword" + i);
			}
			sql.append(" 		)");
			sql.append(" 	)");	
			params.put("itemNo3", "SCHED_MSG");
			// 2.當沒對應資料,則直接找 TSMP_DP_APPT_JOB 中儲存的資料
			sql.append(" 	or (");
			sql.append(" 		not exists (");
			sql.append(" 			select 1");
			sql.append(" 			from TsmpDpItems I3");
			sql.append(" 			where I3.itemNo = :itemNo3");
			sql.append(" 			and I3.subitemNo = J.jobStep");
			sql.append(" 			and I3.locale = :locale");
			sql.append(" 		)");
			for(int i = 0; i < words.length; i++) {
				sql.append(" 		and UPPER(J.jobStep) like :keyword" + i);
			}
			sql.append(" 	)");	
			
			// 執行結果的中文
			// 1.要找到轉換成 TSMP_DP_ITEMS 的 SUBITEM_NAME 的資料
			sql.append(" 	or exists (");
			sql.append(" 		select 1");
			sql.append(" 		from TsmpDpItems I4");
			sql.append(" 		where I4.itemNo = :itemNo4");
			sql.append(" 		and I4.subitemNo = J.execResult");
			sql.append(" 		and I4.locale = :locale");
			sql.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sql.append(" 			or UPPER(I4.subitemName) like :keyword" + i);
			}
			sql.append(" 		)");
			sql.append(" 	)");	
			params.put("itemNo4", "SCHED_MSG");
			// 2.當沒對應資料,則直接找 TSMP_DP_APPT_JOB 中儲存的資料
			sql.append(" 	or (");
			sql.append(" 		not exists (");
			sql.append(" 			select 1");
			sql.append(" 			from TsmpDpItems I4");
			sql.append(" 			where I4.itemNo = :itemNo4");
			sql.append(" 			and I4.subitemNo = J.execResult");
			sql.append(" 			and I4.locale = :locale");
			sql.append(" 		)");
			for(int i = 0; i < words.length; i++) {
				sql.append(" 		and UPPER(J.execResult) like :keyword" + i);
			}
			sql.append(" 	)");	
			
			sql.append(" )");
		}
		sql.append(" order by J.createDateTime desc, J.apptJobId desc");
		
		params.put("sdt", sdt);
		params.put("edt", edt);
		
		return doQuery(sql.toString(), params, TsmpDpApptJob.class, pageSize);
	}

	public List<TsmpDpApptJob> query_dpb0104Service_1(String apptRjobId, TsmpDpApptJob lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sb = new StringBuffer();
		sb.append(" select JOB");
		sb.append(" from TsmpDpApptJob JOB");
		sb.append(" where JOB.periodUid = :periodUid");
		sb.append(" and JOB.periodNexttime is not null");
		// 分頁
		if (lastRecord != null) {
			Long lr_pnt = lastRecord.getPeriodNexttime();
			sb.append(" and ( 1 = 2");
			sb.append(" 	or (JOB.periodNexttime < :periodNexttime and JOB.apptJobId <> :lr_apptJobId)");
			sb.append(" 	or (JOB.periodNexttime = :periodNexttime and JOB.apptJobId < :lr_apptJobId)");
			sb.append(" )");
			params.put("periodNexttime", lastRecord.getPeriodNexttime());
			params.put("lr_apptJobId", lastRecord.getApptJobId());
		}
		sb.append(" order by JOB.periodNexttime desc, JOB.apptJobId desc");
		params.put("periodUid", apptRjobId);
		
		return doQuery(sb.toString(), params, TsmpDpApptJob.class, pageSize);
	}

	public List<TsmpDpApptJob> queryRunLoopJobByFileName(String refSubitemNo, String fileName, List<String> statusList) {
		StringBuffer sb = new StringBuffer();
		Map<String, Object> params = new HashMap<>();
		sb.append(" SELECT j");
		sb.append(" FROM TsmpDpApptJob j");
		sb.append(" WHERE 1 = 1");
		sb.append(" AND j.refItemNo = :refItemNo");
		if (StringUtils.hasText(refSubitemNo)) {
			sb.append(" AND j.refSubitemNo = :refSubitemNo");
			params.put("refSubitemNo", refSubitemNo);
		}
		sb.append(" AND j.status IN :statusList");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM TsmpDpFile f");
		sb.append(" 	WHERE 1 = 1");
		sb.append(" 	AND f.refFileCateCode = :refFileCateCode");
		sb.append(" 	AND f.refId = j.apptJobId");
		sb.append(" 	AND f.fileName = :fileName");
		sb.append(" )");
		params.put("refItemNo", ApptJobEnum.RUNLOOP_ITEM_NO);
		params.put("statusList", statusList);
		params.put("refFileCateCode", TsmpDpFileType.TSMP_DP_APPT_JOB.value());
		params.put("fileName", fileName);
		return doQuery(sb.toString(), params, TsmpDpApptJob.class);
	}

}
