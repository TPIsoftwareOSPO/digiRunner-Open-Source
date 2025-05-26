package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.SeqStore;

public class SeqStoreDaoImpl extends BaseDao {
	// add custom methods here
	
	@PersistenceContext
	private EntityManager em;

	@Transactional
	public Long nextSequence(String sequenceName, Long initial, Long increment) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append("select s.* from seq_store s where s.sequence_name = :sequenceName");
		params.put("sequenceName", sequenceName);
		final List<SeqStore> seqList = doNativeQuery(sb.toString(), null, params, null, SeqStore.class);
		params.clear();
		sb.setLength(0);

		Long nextVal = null;
		if (seqList == null || seqList.isEmpty()) {
			nextVal = initial;
			sb.append("insert into seq_store (sequence_name, next_val) values (:sequenceName, :nextVal)");
			params.put("sequenceName", sequenceName);
			params.put("nextVal", (initial + increment));
		} else {
			SeqStore seq = seqList.get(0);
			final Long oldVal = seq.getNextVal();

			// 否則 hibernate first-level cache 會造成無法更新值
			em.detach(seq);

			nextVal = oldVal;
			sb.append("update seq_store set next_val = :nextVal where sequence_name = :sequenceName and next_val = :oldVal");
			params.put("nextVal", (oldVal + increment));
			params.put("sequenceName", sequenceName);
			params.put("oldVal", oldVal);
		}

		int cnt = 0;
		try {
			cnt = doNativeUpdate(sb.toString(), params);
			if (cnt > 0) {
				return nextVal;
			}
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
		}
		return null;
	}

	public List<SeqStore> queryExpiredSequence(String reqParam, String today_yyyyMMdd) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select S");
		sb.append(" from SeqStore S");
		sb.append(" where S.sequenceName like :reqParam");
		sb.append(" and S.sequenceName <> :todaySequence");
		sb.append(" order by S.sequenceName asc");
		params.put("reqParam", reqParam + "-%");
		params.put("todaySequence", reqParam + "-" + today_yyyyMMdd);
		
		return doQuery(sb.toString(), params, SeqStore.class);
	}

}