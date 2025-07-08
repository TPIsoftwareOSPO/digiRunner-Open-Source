package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrWebhookNotify;

public class DgrWebhookNotifyDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<DgrWebhookNotify> findByDgrWebhookNotifyIdAndKeyword(Long dgrWebsiteId, String enable, String[] words,
			Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

	    StringBuilder sb = new StringBuilder();
	    sb.append(" SELECT w ");
	    sb.append(" FROM DgrWebhookNotify w ");
		sb.append(" WHERE 1 = 1  ");
	    
	    if (dgrWebsiteId != null) {
	        sb.append(" AND ");
	        sb.append(" ( ");
	        sb.append("    1 = 2 ");
	        sb.append("    OR w.webhookNotifyId > :webhookNotifyId ");
	        sb.append(" ) ");
	        params.put("webhookNotifyId", dgrWebsiteId);
	    }

	    if (StringUtils.hasText(enable) && !"-1".equals(enable)) {
	        sb.append(" AND ");
	        sb.append(" ( ");
	        sb.append("    1 = 2 ");
	        sb.append("    OR w.enable = :enable ");
	        sb.append(" ) ");
	        params.put("enable", enable);
	    }
	    
	    if (words != null && words.length > 0) {
	        sb.append(" AND ( 1 = 2 ");
	        for (int i = 0; i < words.length; i++) {
	            sb.append(" OR UPPER(w.notifyName) like :keyworkSearch" + i);
	            sb.append(" OR UPPER(w.notifyType) like :keyworkSearch" + i);
	            sb.append(" OR UPPER(w.createUser) like :keyworkSearch" + i);
	            params.put("keyworkSearch" + i, "%" + words[i].toUpperCase() + "%");
	        }
	        sb.append(" ) ");
	    }

	    sb.append(" ORDER BY w.webhookNotifyId ASC ");

	    return doQuery(sb.toString(), params, DgrWebhookNotify.class, pageSize);
	}
}
