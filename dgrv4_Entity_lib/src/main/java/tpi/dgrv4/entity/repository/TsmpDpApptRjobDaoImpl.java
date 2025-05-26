package tpi.dgrv4.entity.repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpRjobStatus;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;

public class TsmpDpApptRjobDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpApptRjob> queryAvailableRjobs() {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select R");
		sb.append(" from TsmpDpApptRjob R");
		sb.append(" where R.status in (:statusList)");
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpDpApptRjobD D");
		sb.append(" 	where D.apptRjobId = R.apptRjobId");
		sb.append(" )");
		params.put("statusList", Arrays.asList(new String[] {
			TsmpDpRjobStatus.ACTIVE.value(),
			TsmpDpRjobStatus.IN_PROGRESS.value()
		}));
		return doQuery(sb.toString(), params, TsmpDpApptRjob.class);
	}

	public List<TsmpDpApptRjob> query_dpb0102Service_1(String status, TsmpDpApptRjob lastRecord, //
			String[] keywords, String locale, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" select R");
		sb.append(" from TsmpDpApptRjob R");
		sb.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	R.rjobName > :lr_rjobName");
			sb.append(" 	or (R.rjobName = :lr_rjobName and R.apptRjobId > :lr_apptRjobId)");
			sb.append(" )");
			params.put("lr_rjobName", lastRecord.getRjobName());
			params.put("lr_apptRjobId", lastRecord.getApptRjobId());
		}
		// 狀態
		if (!StringUtils.isEmpty(status)) {
			sb.append(" and R.status = :status");
			params.put("status", status);
		}
		// 關鍵字
		if (keywords != null && keywords.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 	or UPPER(R.apptRjobId) like :keyword" + i);
				sb.append(" 	or UPPER(R.rjobName) like :keyword" + i);
				sb.append(" 	or UPPER(R.cronDesc) like :keyword" + i);
				sb.append(" 	or UPPER(R.remark) like :keyword" + i);
				params.put("keyword" + i, "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpDpApptRjobD D");
			sb.append(" 		where D.apptRjobId = R.apptRjobId");
			sb.append(" 		and ( 1 = 2");
			sb.append(" 			or exists (");
			sb.append(" 				select 1");
			sb.append(" 				from TsmpDpItems ITM");
			sb.append(" 				where ITM.itemNo = 'SCHED_CATE1'");
			sb.append(" 				and ITM.subitemNo = D.refItemNo");
			sb.append(" 				and ITM.locale = :locale");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 				and UPPER(ITM.subitemName) like :keyword" + i);
			}
			sb.append(" 			)");
			sb.append(" 			or exists (");
			sb.append(" 				select 1");
			sb.append(" 				from TsmpDpItems ITM");
			sb.append(" 				where ITM.itemNo = D.refItemNo");
			sb.append(" 				and ITM.subitemNo = D.refSubitemNo");
			sb.append(" 				and ITM.locale = :locale");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 				and UPPER(ITM.subitemName) like :keyword" + i);
			}
			sb.append(" 			)");
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by R.rjobName asc, R.apptRjobId asc");
		
		params.put("locale", locale);
		
		return doQuery(sb.toString(), params, TsmpDpApptRjob.class, pageSize);
	}

}