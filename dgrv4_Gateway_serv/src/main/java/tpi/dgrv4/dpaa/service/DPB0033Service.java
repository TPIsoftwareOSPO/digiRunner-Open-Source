package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0033Req;
import tpi.dgrv4.dpaa.vo.DPB0033Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpSiteMap;
import tpi.dgrv4.entity.repository.TsmpDpSiteMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0033Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpSiteMapDao tsmpDpSiteMapDao;

	@Autowired
	public DPB0033Service(TsmpDpSiteMapDao tsmpDpSiteMapDao) {
		super();
		this.tsmpDpSiteMapDao = tsmpDpSiteMapDao;
	}

	public DPB0033Resp addNode(TsmpAuthorization authorization, DPB0033Req req) {
		String userName = authorization.getUserName();
		String desc = req.getSiteDesc();
		String url = req.getSiteUrl();
		if (userName == null || userName.isEmpty() ||
			desc == null || desc.isEmpty() ||
			url == null || url.isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_NODE_REQUIRED.throwing();
		}

		Long siteParentId = req.getSiteParentId();
		// 沒有傳入 siteParentId 表示是要新增根節點
		if (siteParentId == null) {
			TsmpDpSiteMap root = getRoot();
			if (root != null) {
				throw TsmpDpAaRtnCode.FAIL_CREATE_NODE.throwing();
			}
			siteParentId = 0L;
		}

		TsmpDpSiteMap node = saveNode(userName, siteParentId, desc, url);

		DPB0033Resp resp = new DPB0033Resp();
		resp.setSiteId(node.getSiteId());
		resp.setSiteParentId(node.getSiteParentId());
		resp.setSiteDesc(node.getSiteDesc());
		resp.setDataSort(node.getDataSort());
		resp.setSiteUrl(node.getSiteUrl());
		return resp;
	}

	private TsmpDpSiteMap getRoot() {
		Long rootParentId = 0L;	// 根節點的父節點ID為0
		Sort sort = getDefaultSort();
		List<TsmpDpSiteMap> list = getTsmpDpSiteMapDao().findBySiteParentId(rootParentId, sort);
		if (list != null && !list.isEmpty()) {
			// 應該只有一筆
			return list.get(0);
		}
		return null;
	}

	private TsmpDpSiteMap saveNode(String userName, Long siteParentId, String siteDesc, String siteUrl) {
		TsmpDpSiteMap node = new TsmpDpSiteMap();
		node.setSiteParentId(siteParentId);
		node.setSiteDesc(siteDesc);
		Integer dataSort = getDataSort(siteParentId);
		node.setDataSort(dataSort);
		node.setSiteUrl(siteUrl);
		node.setCreateDateTime(DateTimeUtil.now());
		node.setCreateUser(userName);
		node = getTsmpDpSiteMapDao().save(node);
		return node;
	}

	private Integer getDataSort(Long siteParentId) {
		// 根目錄的排序為0
		if (siteParentId == 0L) {
			return 0;
		}

		int maxDataSort = 1;
		try {
			Sort sort = Sort.by(Sort.Direction.DESC, "dataSort");
			List<TsmpDpSiteMap> nodes = getTsmpDpSiteMapDao().findBySiteParentId(siteParentId, sort);
			if (nodes != null && !nodes.isEmpty()) {
				Integer dataSort = nodes.get(0).getDataSort();
				if (dataSort != null) {
					maxDataSort = dataSort + 1;
				}
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		return maxDataSort;
	}

	private Sort getDefaultSort() {
		return Sort.by(Sort.DEFAULT_DIRECTION, "siteParentId", "dataSort", "siteId");
	}

	protected TsmpDpSiteMapDao getTsmpDpSiteMapDao() {
		return this.tsmpDpSiteMapDao;
	}

}
