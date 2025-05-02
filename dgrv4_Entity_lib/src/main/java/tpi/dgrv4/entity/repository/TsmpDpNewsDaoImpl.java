package tpi.dgrv4.entity.repository;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFbFlag;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;

public class TsmpDpNewsDaoImpl extends BaseDao {
	// add custom methods here


	@PersistenceContext
	private EntityManager entityManager;
	
	//DPB0045, DPF0036
	public List<TsmpDpNews> queryId(List<String> orgDescList, Long newsId) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpNews A where 1 = 1");
 
		// 組織
		sb.append(" and (A.orgId is null or length(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");
		// ID
		sb.append(" and A.newsId = :newsId");
		params.put("newsId", newsId);

		return doQuery(sb.toString(), params, TsmpDpNews.class, -1);
	}
 	
	//DPB0044, DPF0037
	public List<TsmpDpNews> queryLike(TsmpDpNews lastRecord, String[] words, Date startDate, Date endDate, Date nowDate,
			String typeItemNo, String enFlag, String fbType, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select news from TsmpDpNews news where 1 = 1");

		// 是否有傳入 last row id
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (news.postDateTime < :lastPdt and news.newsId <> :lastNewsId)");
			sb.append("    or (news.postDateTime = :lastPdt and news.newsId > :lastNewsId)");
			sb.append(" )");			
			params.put("lastPdt", lastRecord.getPostDateTime());
			params.put("lastNewsId", lastRecord.getNewsId());
		}

		sb.append(" and news.postDateTime >= :queryStartDate");//公告日期 >= queryStartDate
		sb.append(" and news.postDateTime < :queryEndDate");//公告日期 < queryEndDate+一天
		params.put("queryStartDate", startDate);
		params.put("queryEndDate", endDate);
		
		if(!enFlag.isEmpty() && !enFlag.equals(TsmpDpDataStatus.ALL.value())) {
			sb.append(" and news.status = :status ");
			params.put("status", enFlag);
		}
		
		//前台要去除 "公告日期" 未到期前的資料: 公告日期 < (今天日期 + 一天)
		if(!fbType.isEmpty() && fbType.equals(TsmpDpFbFlag.FRONT.value())) {//前台
			sb.append(" and news.postDateTime < :nowDate ");//公告日期」< nowDate+一天
			params.put("nowDate", nowDate);
		}
		
		if (StringUtils.hasLength(typeItemNo)) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (news.refTypeSubitemNo = :refTypeSubitemNo)");//公告類型
			sb.append(" )");			
			params.put("refTypeSubitemNo", typeItemNo);
		}
		
		//關鍵字search [NEWS_ID/NEW_TITLE/NEW_CONTENT]
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {

				//Id 要轉型
				Long keywordId = null;
				try {
					keywordId = Long.valueOf(words[i]);
					sb.append("    or news.newsId = :wordId" + i);//Long
					params.put("wordId" + i, keywordId );//Long
				}catch (Exception e) {
				}

				sb.append("    or UPPER(news.newTitle) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(news.newContent) like :word" + i);//忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
			}
			sb.append(" )");
		} 
		sb.append(" order by news.postDateTime desc, news.newsId asc");

		
		return doQuery(sb.toString(), params, TsmpDpNews.class, pageSize);
	}

	/**
	 * 取得過期的公告消息
	 * 
	 * @param expDate
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<TsmpDpNews> query_dpb0046Job(Date expDate) {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A FROM TsmpDpNews A");
		sb.append(" WHERE A.status = :status");
		sb.append(" AND A.updateDateTime < :expDate");
		
		Query query;
		try {
			query = entityManager.createQuery(sb.toString(), TsmpDpNews.class);
			query.setParameter("status", TsmpDpDataStatus.OFF.value());//狀態為0=停用
			query.setParameter("expDate", expDate, TemporalType.DATE);
			return query.getResultList();
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			return Collections.EMPTY_LIST;
		}
	}
 
}
