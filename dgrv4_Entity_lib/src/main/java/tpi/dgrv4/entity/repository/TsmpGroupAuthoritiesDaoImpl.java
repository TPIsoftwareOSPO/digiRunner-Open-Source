package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;

public class TsmpGroupAuthoritiesDaoImpl extends BaseDao {
    
	public List<TsmpGroupAuthorities> queryByIdAndLevelAndKeyword(String lastGroupAuthoritieId, String[] keyword, List<String> selectedGroupAuthoritieIdList, int pageSize){
        Map<String, Object> params = new HashMap<>();
        
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT tsmpGroupAuthorities ");
        sb.append(" FROM TsmpGroupAuthorities tsmpGroupAuthorities ");
        sb.append(" WHERE 1 =1 ");
        
        
        //傳入前端已經選擇的groupAuthoritiesId
        if (selectedGroupAuthoritieIdList!=null && selectedGroupAuthoritieIdList.size() > 0) {
            sb.append(" AND tsmpGroupAuthorities.groupAuthoritieId NOT IN :selectedGroupAuthoritieIdList ");
            params.put("selectedGroupAuthoritieIdList", selectedGroupAuthoritieIdList);
        }
        
        //分頁
        if (StringUtils.isEmpty(lastGroupAuthoritieId)==false) {
            sb.append(" AND ");
            sb.append(" ( 1=2 ");
            sb.append("   OR (tsmpGroupAuthorities.groupAuthoritieId > :lastGroupAuthoritieId ) ");
            sb.append("  ) ");
            params.put("lastGroupAuthoritieId", lastGroupAuthoritieId);
        }
        
        //關鍵字
        if (keyword!=null && keyword.length > 0) {
            sb.append(" AND ");
            sb.append(" ( 1=2 ");
            for (int i = 0; i < keyword.length; i++) {
                sb.append(" OR tsmpGroupAuthorities.groupAuthoritieId = :keywordSearchGroupAuthoritieId"+i);
                params.put("keywordSearchGroupAuthoritieId"+i, keyword[i]);
                
                               
                sb.append(" OR UPPER(tsmpGroupAuthorities.groupAuthoritieName) LIKE :keywordSearch"+i);
                sb.append(" OR UPPER(tsmpGroupAuthorities.groupAuthoritieDesc) LIKE :keywordSearch"+i);
                sb.append(" OR UPPER(tsmpGroupAuthorities.groupAuthoritieLevel) LIKE :keywordSearch"+i);
                params.put("keywordSearch"+i, "%"+keyword[i].toUpperCase()+"%");
            }
            sb.append("  ) ");
        }
        
        sb.append(" ORDER BY tsmpGroupAuthorities.groupAuthoritieId ASC ");
        
        return doQuery(sb.toString(), params, TsmpGroupAuthorities.class, pageSize);
    }
}
