package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrWebsite;

public class DgrWebsiteDaoImpl extends BaseDao{

	private final String dgrWebsiteIdStr = "dgrWebsiteId";
	private final String websiteStatusStr = "websiteStatus";
	private final String keyworkSearchStr = "keyworkSearch";
	
	public List<DgrWebsite> findByDgrWebsiteIdAndKeyword(Long dgrWebsiteId, String websiteStatus, String[] words,
			Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

	    StringBuilder sb = new StringBuilder();
	    sb.append(" SELECT w ");
	    sb.append(" FROM DgrWebsite w ");
		sb.append(" WHERE 1 = 1  ");
	    
	    if (dgrWebsiteId != null) {
	        sb.append(" AND ");
	        sb.append(" ( ");
	        sb.append("    1 = 2 ");
	        sb.append("    OR w.dgrWebsiteId > :dgrWebsiteId ");
	        sb.append(" ) ");
	        params.put(dgrWebsiteIdStr, dgrWebsiteId);
	    }

	    if (StringUtils.hasText(websiteStatus) && !"-1".equals(websiteStatus)) {
	        sb.append(" AND ");
	        sb.append(" ( ");
	        sb.append("    1 = 2 ");
	        sb.append("    OR w.websiteStatus = :websiteStatus ");
	        sb.append(" ) ");
	        params.put(websiteStatusStr, websiteStatus);
	    }
	    
	    if (words != null && words.length > 0) {
	        sb.append(" AND ( 1 = 2 ");
	        for (int i = 0; i < words.length; i++) {
	            sb.append(" OR UPPER(w.websiteName) like :keyworkSearch" + i);
	            sb.append(" OR UPPER(w.remark) like :keyworkSearch" + i);
	            sb.append(" OR EXISTS ( ");
	            sb.append("        SELECT 1 ");
	            sb.append("        FROM DgrWebsiteDetail wd ");
	            sb.append("        WHERE w.dgrWebsiteId = wd.dgrWebsiteId ");
	            sb.append("        AND ( 1 = 2 ");
	            sb.append("            OR UPPER(wd.url) like :keyworkSearch" + i);
	            sb.append("        ) ");
	            sb.append("    ) ");
	            params.put(keyworkSearchStr + i, "%" + words[i].toUpperCase() + "%");
	        }
	        sb.append(" ) ");
	    }

	    sb.append(" ORDER BY w.dgrWebsiteId ASC ");

	    return doQuery(sb.toString(), params, DgrWebsite.class, pageSize);
	}
	
}
