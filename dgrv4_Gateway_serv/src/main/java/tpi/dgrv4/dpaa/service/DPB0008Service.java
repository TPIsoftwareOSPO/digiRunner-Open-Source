package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB0008AppCate;
import tpi.dgrv4.dpaa.vo.DPB0008Req;
import tpi.dgrv4.dpaa.vo.DPB0008Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0008Service {

	private TsmpDpAppCategoryDao tsmpDpAppCategoryDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0008Service(TsmpDpAppCategoryDao tsmpDpAppCategoryDao, TsmpOrganizationDao tsmpOrganizationDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpDpAppCategoryDao = tsmpDpAppCategoryDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0008Resp queryAppCateLikeList(TsmpAuthorization authorization, DPB0008Req req) {
		String userName = authorization.getUserName();
		String orgId = authorization.getOrgId();
		if (userName == null || userName.isEmpty() ||
			orgId == null || orgId.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_TSMP_USER.throwing();
		}

		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		List<TsmpDpAppCategory> cateList = getCateList(req, orgDescList);
		if (cateList == null || cateList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_APP_CATE_DATA.throwing();
		}

		DPB0008Resp resp = new DPB0008Resp();
		List<DPB0008AppCate> dpb0008AppCateList = getDpb0008AppCateList(cateList);
		resp.setAppCateList(dpb0008AppCateList);
		return resp;
	}

	private List<TsmpDpAppCategory> getCateList(DPB0008Req req, List<String> orgDescList) {
		List<TsmpDpAppCategory> cateList = null;

		TsmpDpAppCategory lastRecord = getLastRecordFromPrevPage(req.getAppCateId());
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		cateList = getTsmpDpAppCategoryDao().queryLikeAndSort(orgDescList, words, lastRecord, pageSize);

		return cateList;
	}

	private TsmpDpAppCategory getLastRecordFromPrevPage(Long appCateId) {
		if (appCateId != null) {
			Optional<TsmpDpAppCategory> opt = getTsmpDpAppCategoryDao().findById(appCateId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0008AppCate> getDpb0008AppCateList(List<TsmpDpAppCategory> cateList) {
		List<DPB0008AppCate> dpb0008AppCateList = new ArrayList<>();

		Optional<String> opt;
		DPB0008AppCate dpb0008AppCate;
		for(TsmpDpAppCategory cate : cateList) {
			dpb0008AppCate = new DPB0008AppCate();
			dpb0008AppCate.setAppCateId(cate.getAppCateId());
			dpb0008AppCate.setAppCateName(cate.getAppCateName());
			dpb0008AppCate.setDataSort(cate.getDataSort());

			opt = DateTimeUtil.dateTimeToString(cate.getCreateDateTime(), null);
			if (opt.isPresent()) {
				dpb0008AppCate.setCreateDateTime(opt.get());
			}

			opt = DateTimeUtil.dateTimeToString(cate.getUpdateDateTime(), null);
			if (opt.isPresent()) {
				dpb0008AppCate.setUpdateDateTime(opt.get());
			}

			dpb0008AppCateList.add(dpb0008AppCate);
		}

		return dpb0008AppCateList;
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpAppCategoryDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0008");
		return this.pageSize;
	}

}
