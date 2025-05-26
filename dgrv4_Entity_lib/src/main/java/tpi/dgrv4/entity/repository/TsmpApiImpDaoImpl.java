package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class TsmpApiImpDaoImpl extends BaseDao {
	// add custom methods here

	public Integer queryMaxBatchNo() {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT MAX(A.batchNo)");
		sb.append(" FROM TsmpApiImp A");
		
		List<Integer> dataList = doQuery(sb.toString(), params, Integer.class);
		return (CollectionUtils.isEmpty(dataList) ? null : dataList.get(0));
	}

}