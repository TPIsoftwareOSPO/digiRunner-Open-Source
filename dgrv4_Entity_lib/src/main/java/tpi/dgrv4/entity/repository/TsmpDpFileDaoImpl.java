package tpi.dgrv4.entity.repository;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.entity.entity.TsmpDpFile;

import java.util.*;

public class TsmpDpFileDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpDpFile> queryByRefFileCateCodeAndRefId(String refFileCateCode ,Long refId){
		
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT new tpi.dgrv4.entity.entity.TsmpDpFile( ");
		sql.append(" 	t.refId, ");
		sql.append(" 	t.fileId, ");
		sql.append(" 	t.fileName, ");
		sql.append(" 	t.createDateTime");
		sql.append(" ) ");
		sql.append(" FROM TsmpDpFile t ");
		sql.append(" WHERE 1 = 1 AND ");
		sql.append(" refFileCateCode = :refFileCateCode AND ");
		sql.append(" refId = :refId  ");
		sql.append(" ORDER BY fileId DESC ");
		

		params.put("refId", refId);
		params.put("refFileCateCode", refFileCateCode);
		
		return doQuery(sql.toString(), params, TsmpDpFile.class);
	}

	public List<TsmpDpFile> query_DPB0061Service_01(String refFileCateCode, String fileNameSuffix) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT f");
		sb.append(" FROM TsmpDpFile f");
		sb.append(" WHERE f.refFileCateCode = :refFileCateCode");
		sb.append(" AND f.isTmpfile = :isTmpFile");
		sb.append(" AND f.isBlob = :isBlob");
		sb.append(" AND f.fileName LIKE :fileName");
		params.put("refFileCateCode", refFileCateCode);
		params.put("isTmpFile", "Y");
		params.put("isBlob", "Y");
		params.put("fileName", "%" + fileNameSuffix);
		return doQuery(sb.toString(), params, TsmpDpFile.class);
	}

	public List<TsmpDpFile> query_DPB9915Service_01(Long lastId, Date lastDateTime, Date startDate, Date endDate,ArrayList<Long> fileIds, //
			List<String> fileNames, String refFileCateCode, Long refId, String isTempFile, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT new tpi.dgrv4.entity.entity.TsmpDpFile( ");
		sb.append("     f.fileId, f.fileName, f.filePath, f.refFileCateCode, f.refId, f.isBlob, ");
		sb.append("     f.isTmpfile, f.createDateTime, f.createUser, f.updateDateTime, f.updateUser, f.version)");
		sb.append(" FROM TsmpDpFile f ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND ( ");
		sb.append("      1 = 2 ");
		sb.append("      OR ( ");
		sb.append("          f.updateDateTime IS NOT NULL ");
		sb.append("          AND f.updateDateTime BETWEEN  :startDate AND  :endDate ");

		if (lastId != null) {
			sb.append("          AND ( ");
			sb.append("               1 = 2 ");
			sb.append("               OR f.updateDateTime < :lastDateTime ");
			sb.append("               OR (f.updateDateTime = :lastDateTime AND f.fileId > :lastId )");
			sb.append("          )");
		}

		sb.append("      )");
		sb.append("      OR ( ");
		sb.append("			 f.updateDateTime IS NULL AND f.createDateTime IS NOT NULL ");
		sb.append("          AND f.createDateTime BETWEEN :startDate AND :endDate ");

		if (lastId != null) {
			sb.append("          AND ( ");
			sb.append("               1 = 2 ");
			sb.append("               OR f.createDateTime < :lastDateTime ");
			sb.append("               OR (f.createDateTime = :lastDateTime AND f.fileId > :lastId ) ");
			sb.append("         )");
		}
		sb.append("      )");
		sb.append(" )");

		if (StringUtils.hasLength(refFileCateCode)) {
			sb.append(" AND f.refFileCateCode = :reqRefFileCateCode ");
		}

		if (refId != null) {
			sb.append(" AND f.refId = :refId ");
		}

		if (StringUtils.hasLength(isTempFile) && isTempFile.equals("Y")) {
			sb.append(" AND f.isTmpfile = 'Y' ");
		}

		if (!StringUtils.hasLength(isTempFile) || isTempFile.equals("N")) {
			sb.append(" AND (f.isTmpfile IS NULL OR f.isTmpfile = 'N') ");
		}

		// -- [BEGIN] 有帶入 DPB9915Req.keyword 才加下面這一段
		int count = 0;
		if (!CollectionUtils.isEmpty(fileNames)) {
			sb.append(" AND ( ");
			sb.append(" 1 = 2 ");

			if (!CollectionUtils.isEmpty(fileIds)) {
				for (Long fileId : fileIds) {
					sb.append(" OR f.fileId = :fileId" + count + " ");
					params.put("fileId" + count, fileId);
					count++;
				}
			}
			count = 0;

			for (String string : fileNames) {
				string = string.toUpperCase();
				sb.append(" OR UPPER(f.fileName) LIKE :fileName"+count);
				params.put("fileName" + count, "%"+string+"%");
				count++;
			}
			sb.append(" ) ");
		}

		sb.append(" ORDER BY ( ");
		sb.append("   CASE WHEN f.updateDateTime IS NOT NULL ");
		sb.append("   THEN f.updateDateTime ");
		sb.append("   ELSE f.createDateTime ");
		sb.append("   END ");
		sb.append(" ) DESC, f.fileId ASC ");
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("lastDateTime", lastDateTime);
		params.put("lastId", lastId);
		params.put("reqRefFileCateCode", refFileCateCode);
		params.put("refId", refId);
		

		return doQuery(sb.toString(), params, TsmpDpFile.class, pageSize);
	}

	public List<Integer> query_DPB9915Service_02(Long fileId){
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append("SELECT CASE WHEN (f.isBlob = 'Y' AND f.blobData IS NOT NULL) THEN 1 ELSE 0 END ");
	    sb.append("FROM TsmpDpFile f ");
	    sb.append("WHERE f.fileId = :fileId");
		
		params.put("fileId", fileId);

		List<Integer> results = doQuery(sb.toString(), params, Integer.class);
		if (results.isEmpty()) {
			return Collections.singletonList(0);
		} else {
			Integer result = results.get(0);
			if (result != null && result.intValue() == 1) {
				return Collections.singletonList(1);
			} else {
				return Collections.singletonList(0);
			}
		}
	}

}
