package tpi.dgrv4.entity.repository;

import org.springframework.util.StringUtils;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;
import tpi.dgrv4.entity.vo.DPB0304SearchCriteria;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DgrH2ConfigSyncHistoryDaoImpl extends BaseDao{

    public List<DgrH2ConfigSyncHistory> query_DPB0304Service(DPB0304SearchCriteria criteria) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT m FROM DgrH2ConfigSyncHistory m ");
        sb.append("WHERE 1 = 1 ");

        if (StringUtils.hasText(criteria.getSyncType())) {
            sb.append("AND m.syncType = :syncType ");
            params.put("syncType", criteria.getSyncType());
        }

        // ========== 狀態過濾 ==========
        if (StringUtils.hasText(criteria.getStatus())) {
            sb.append("AND m.status = :status ");
            params.put("status", criteria.getStatus());
        }

        // ========== 關鍵字搜尋（來源 ID、目標 ID） ==========
        String[] keywords = criteria.getKeyword();
        if (keywords != null && keywords.length > 0) {
        sb.append(" AND ( ");
        sb.append("		1=2 ");
            for (int i = 0; i < keywords.length; i++) {
                sb.append("  OR UPPER(m.sourceId) LIKE :keyword" + i);
                sb.append("  OR UPPER(m.targetIds) LIKE :keyword" + i);
                sb.append("  OR CONCAT(m.syncId, '') LIKE :keyword" + i);
                sb.append("  OR CONCAT(m.scheduleId, '') LIKE :keyword" + i);
                sb.append(") ");
            params.put("keyword" + i, "%" + keywords[i].toUpperCase() + "%");

        }
//            sb.append(" 	) ");
        }
        // ========== 分頁 ==========
        DgrH2ConfigSyncHistory lastId = criteria.getLastId();
        if (lastId != null) {
            // 使用複合條件實作游標分頁
            // 根據排序欄位決定分頁條件
            Map<String, String> sortBy = criteria.getSortBy();

            if (sortBy != null && !sortBy.isEmpty()) {
                // 有自訂排序
                String firstSortKey = sortBy.keySet().iterator().next();
                String firstSortOrder = sortBy.get(firstSortKey);

                if ("startTime".equals(firstSortKey)) {
                    // 以開始時間排序
                    if ("desc".equalsIgnoreCase(firstSortOrder)) {
                        sb.append("AND (");
                        sb.append("  m.startTime < :lastStartTime ");
                        sb.append("  OR (m.startTime = :lastStartTime AND m.syncId < :lastSyncId) ");
                        sb.append(") ");
                    } else {
                        sb.append("AND (");
                        sb.append("  m.startTime > :lastStartTime ");
                        sb.append("  OR (m.startTime = :lastStartTime AND m.syncId > :lastSyncId) ");
                        sb.append(") ");
                    }
                    params.put("lastStartTime", lastId.getStartTime());
                    params.put("lastSyncId", lastId.getSyncId());

                } else if ("syncId".equals(firstSortKey)) {
                    // 以 syncId 排序
                    if ("desc".equalsIgnoreCase(firstSortOrder)) {
                        sb.append("AND m.syncId < :lastSyncId ");
                    } else {
                        sb.append("AND m.syncId > :lastSyncId ");
                    }
                    params.put("lastSyncId", lastId.getSyncId());

                } else {
                    // 其他欄位：預設以 syncId 分頁
                    sb.append("AND m.syncId > :lastSyncId ");
                    params.put("lastSyncId", lastId.getSyncId());
                }
            } else {
                // 預設排序（startTime desc, syncId desc）
                sb.append("AND (");
                sb.append("  m.startTime < :lastStartTime ");
                sb.append("  OR (m.startTime = :lastStartTime AND m.syncId < :lastSyncId) ");
                sb.append(") ");
                params.put("lastStartTime", lastId.getStartTime());
                params.put("lastSyncId", lastId.getSyncId());
            }
        }

        // ========== 排序 ==========
        Map<String, String> sortBy = criteria.getSortBy();
        if (sortBy != null && !sortBy.isEmpty()) {
            sb.append("ORDER BY ");

            int index = 0;
            for (Map.Entry<String, String> entry : sortBy.entrySet()) {
                if (index > 0) {
                    sb.append(", ");
                }

                String field = entry.getKey();
                String order = entry.getValue();

                // 將欄位名稱轉換為 Entity 屬性名稱
                String entityField = convertToEntityField(field);
                sb.append("m.").append(entityField).append(" ").append(order);

                index++;
            }

            // 總是加上 syncId 作為次要排序（保證穩定排序）
            if (!sortBy.containsKey("syncId")) {
                String firstOrder = sortBy.values().iterator().next();
                sb.append(", m.syncId ").append(firstOrder);
            }
        } else {
            // 預設排序：開始時間降冪，syncId 降冪
            sb.append("ORDER BY m.startTime DESC, m.syncId DESC");
        }

        return doQuery(sb.toString(), params, DgrH2ConfigSyncHistory.class, criteria.getPageSize());
    }


    private String convertToEntityField(String field) {

        switch (field) {
            case "syncId":
                return "syncId";
            case "syncType":
                return "syncType";
            case "status":
                return "status";
            case "startTime":
                return "startTime";
            case "endTime":
                return "endTime";
            default:
                return field;
        }
    }

}
