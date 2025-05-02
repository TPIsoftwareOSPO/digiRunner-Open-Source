package tpi.dgrv4.entity.repository;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Query;
import tpi.dgrv4.entity.entity.TsmpApiReg;

public class TsmpApiRegDaoImpl extends BaseDao {

	public int deleteNonSpecifiedContent(List<AbstractMap.SimpleEntry<String, String>> list) {

		if (list == null) {
			return 0;
		}

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();

		sb.append(" DELETE FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE NOT ( tsmpApiReg.apiKey = '' AND  tsmpApiReg.moduleName ='' ");

		for (int i = 0; i < list.size(); i++) {

			SimpleEntry<String, String> entry = list.get(i);
			String apiKey = entry.getKey();
			String moduleName = entry.getValue();

			String paramsApiKey = "apiKey" + i;
			String paramsModuleName = "moduleName" + i;

			sb.append(" OR ( tsmpApiReg.apiKey =:" + paramsApiKey);
			sb.append(" AND tsmpApiReg.moduleName =:" + paramsModuleName + " )");

			params.put(paramsApiKey, apiKey);
			params.put(paramsModuleName, moduleName);
		}

		sb.append(" )");

		Query query = getEntityManager().createQuery(sb.toString());

		for (Map.Entry<String, Object> param : params.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}

		return query.executeUpdate();
	}

	public List<String> query_AA0429SrcUrl() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpApiReg.srcUrl ");
		sb.append(" FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" SELECT  1 FROM TsmpApi a");
		sb.append("  WHERE  a.apiSrc = 'R'	");

		sb.append("  AND a.apiKey = tsmpApiReg.apiKey ");
		sb.append(" AND a.moduleName  = tsmpApiReg.moduleName ");
		sb.append(" )");
		sb.append(" AND tsmpApiReg.srcUrl IS NOT NULL");
//		sb.append(" AND tsmpApiReg.srcUrl <> ''");
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0429IpSrcUrl1() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpApiReg.ipSrcUrl1 ");
		sb.append(" FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT  1 FROM TsmpApi a");
		sb.append("  		WHERE  a.apiSrc = 'R'");

		sb.append("  		AND a.apiKey = tsmpApiReg.apiKey ");
		sb.append(" 		AND a.moduleName  = tsmpApiReg.moduleName ");
		sb.append(" )");
		sb.append(" AND tsmpApiReg.ipSrcUrl1 IS NOT NULL");
//		sb.append(" AND tsmpApiReg.ipSrcUrl1 <> ''");
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0429IpSrcUrl2() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpApiReg.ipSrcUrl2 ");
		sb.append(" FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT  1 FROM TsmpApi a");
		sb.append(" 		WHERE  a.apiSrc = 'R'");

		sb.append(" 		AND a.apiKey = tsmpApiReg.apiKey ");
		sb.append(" 		AND a.moduleName  = tsmpApiReg.moduleName ");
		sb.append(" )");
		sb.append(" AND tsmpApiReg.ipSrcUrl2 IS NOT NULL");
//		sb.append(" AND tsmpApiReg.ipSrcUrl2 <> ''");
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0429IpSrcUrl3() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpApiReg.ipSrcUrl3 ");
		sb.append(" FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT  1 FROM TsmpApi a");
		sb.append("  		WHERE  a.apiSrc = 'R'");

		sb.append("  		AND a.apiKey = tsmpApiReg.apiKey ");
		sb.append(" 		AND a.moduleName  = tsmpApiReg.moduleName ");
		sb.append(" )");
		sb.append(" AND tsmpApiReg.ipSrcUrl3 IS NOT NULL");
//		sb.append(" AND tsmpApiReg.ipSrcUrl3 <> ''");
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0429IpSrcUrl4() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpApiReg.ipSrcUrl4 ");
		sb.append(" FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT  1 FROM TsmpApi a");
		sb.append("  		WHERE  a.apiSrc = 'R'");

		sb.append("  		AND a.apiKey = tsmpApiReg.apiKey ");
		sb.append(" 		AND a.moduleName  = tsmpApiReg.moduleName ");
		sb.append(" )");
		sb.append(" AND tsmpApiReg.ipSrcUrl4 IS NOT NULL");
//		sb.append(" AND tsmpApiReg.ipSrcUrl4 <> ''");
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0429IpSrcUrl5() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpApiReg.ipSrcUrl5 ");
		sb.append(" FROM TsmpApiReg tsmpApiReg ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT  1 FROM TsmpApi a");
		sb.append("  		WHERE  a.apiSrc = 'R'");

		sb.append("  		AND a.apiKey = tsmpApiReg.apiKey ");
		sb.append(" 		AND a.moduleName  = tsmpApiReg.moduleName ");
		sb.append(" )");
		sb.append(" AND tsmpApiReg.ipSrcUrl5 IS NOT NULL");
//		sb.append(" AND tsmpApiReg.ipSrcUrl5 <> ''");
		return doQuery(sb.toString(), params, String.class);
	}

	public List<TsmpApiReg> query_AA0423Service() {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT r FROM TsmpApiReg r ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT  1 FROM TsmpApi a");
		sb.append("  		WHERE  a.apiSrc = 'R'");
		sb.append("  		AND a.apiKey = r.apiKey ");
		sb.append(" 		AND a.moduleName  = r.moduleName ");
		sb.append(" )");

		return doQuery(sb.toString(), params, TsmpApiReg.class);
	}

}
