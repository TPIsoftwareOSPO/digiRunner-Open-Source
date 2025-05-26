package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.common.utils.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0027Faq;
import tpi.dgrv4.dpaa.vo.DPB0027Req;
import tpi.dgrv4.dpaa.vo.DPB0027Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqAnswer;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqQuestion;
import tpi.dgrv4.entity.repository.TsmpDpFaqAnswerDao;
import tpi.dgrv4.entity.repository.TsmpDpFaqQuestionDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0027Service {

	private TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao;
	private TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao;
	private ServiceConfig serviceConfig;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	private Integer pageSize;

	@Autowired
	public DPB0027Service(TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao, TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao,
			ServiceConfig serviceConfig, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpFaqQuestionDao = tsmpDpFaqQuestionDao;
		this.tsmpDpFaqAnswerDao = tsmpDpFaqAnswerDao;
		this.serviceConfig = serviceConfig;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0027Resp queryFaqLikeList_0(TsmpAuthorization authorization, DPB0027Req req, ReqHeader reqHeader) {
		List<String> dataStatusList = getDataStatusList(req.getDataStatus());
		if (dataStatusList.isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_QUERY_FAQ_REQUIRED.throwing();
		}
		
		TsmpDpFaqQuestion lastRecord = getLastRecordFromPrevPage(req.getQuestionId(), req.getDataSort());
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		List<TsmpDpFaqQuestion> qList = getTsmpDpFaqQuestionDao() //
				.query_dpb0027Service(dataStatusList, words, lastRecord, pageSize);
		if (qList == null || qList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_FAQ_DATA.throwing();
		}

		DPB0027Resp resp = new DPB0027Resp();
		List<DPB0027Faq> dpb0027FaqList = getDpb0027FaqList(qList, reqHeader.getLocale());
		resp.setFaqList(dpb0027FaqList);
		return resp;
	}

	private List<String> getDataStatusList(String dataStatus) {
		List<String> dataStatusList = new ArrayList<>();

		if (dataStatus != null) {
			if (dataStatus.isEmpty()) {
				dataStatusList.add(TsmpDpDataStatus.ON.value());
				dataStatusList.add(TsmpDpDataStatus.OFF.value());
			} else {
				dataStatusList.add(dataStatus);
			}
		}

		return dataStatusList;
	}

	private TsmpDpFaqQuestion getLastRecordFromPrevPage(Long questionId, Integer dataSort) {
		if (questionId != null) {
			if (dataSort == null) {
				Optional<TsmpDpFaqQuestion> opt = getTsmpDpFaqQuestionDao().findById(questionId);
				if (opt.isPresent()) {
					return opt.get();
				}
			} else {
				TsmpDpFaqQuestion lastRecord = new TsmpDpFaqQuestion();
				lastRecord.setQuestionId(questionId);
				lastRecord.setDataSort(dataSort);
				return lastRecord;
			}
		}
		return null;
	}

	private List<DPB0027Faq> getDpb0027FaqList(List<TsmpDpFaqQuestion> qList, String locale) {
		List<DPB0027Faq> dpb0027FaqList = new ArrayList<>();

		TsmpDpFaqAnswer a;
		DPB0027Faq dpb0027Faq;
		for(TsmpDpFaqQuestion q : qList){
			dpb0027Faq = new DPB0027Faq();
			dpb0027Faq.setQuestionId(q.getQuestionId());
			dpb0027Faq.setQuestionName(q.getQuestionName());
			dpb0027Faq.setDataSort(q.getDataSort());
			TsmpDpItems vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("ENABLE_FLAG", q.getDataStatus(), locale);
			String dataStatus = "";
			if(vo != null) {
				dataStatus = vo.getSubitemName();// 狀態中文
			}
			dpb0027Faq.setDataStatus(dataStatus);
			a = getAnswer(q.getQuestionId());
			if (a != null) {
				dpb0027Faq.setAnswerId(a.getAnswerId());
				dpb0027Faq.setAnswerName(a.getAnswerName());
			}
			dpb0027FaqList.add(dpb0027Faq);
		}

		return dpb0027FaqList;
	}

	private TsmpDpFaqAnswer getAnswer(Long refQuestionId) {
		if (refQuestionId != null) {
			List<TsmpDpFaqAnswer> aList = getTsmpDpFaqAnswerDao().findByRefQuestionId(refQuestionId);
			if (aList != null && !aList.isEmpty()) {
				return aList.get(0);
			}
		}
		return null;
	}

	protected TsmpDpFaqQuestionDao getTsmpDpFaqQuestionDao() {
		return this.tsmpDpFaqQuestionDao;
	}

	protected TsmpDpFaqAnswerDao getTsmpDpFaqAnswerDao() {
		return this.tsmpDpFaqAnswerDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0027");
		return this.pageSize;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
	
	

}
