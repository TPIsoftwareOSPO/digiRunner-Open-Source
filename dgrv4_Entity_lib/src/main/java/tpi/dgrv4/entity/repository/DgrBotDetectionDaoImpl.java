package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DgrBotDetectionDaoImpl extends BaseDao {

	public List<Long> findBotDetectionIdByTypeOrderByCreateDateTimeDescBotDetectionIdDesc(String type) {
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);

		String sql = "SELECT A.botDetectionId FROM DgrBotDetection A WHERE A.type = :type ORDER BY A.createDateTime DESC, A.botDetectionId DESC";

		return doQuery(sql, params, Long.class);
	}

	public List<Long> findBotDetectionIdByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(String type) {
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);

		String sql = "SELECT A.botDetectionId FROM DgrBotDetection A WHERE A.type = :type ORDER BY A.createDateTime ASC, A.botDetectionId ASC";

		return doQuery(sql, params, Long.class);
	}

}
