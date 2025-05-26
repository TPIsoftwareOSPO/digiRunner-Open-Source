package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0158ItemReq;
import tpi.dgrv4.dpaa.vo.DPB0158Req;
import tpi.dgrv4.dpaa.vo.DPB0158Resp;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0158Service {

	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0158Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
	}

	@Transactional
	public DPB0158Resp getWebsiteInfo(TsmpAuthorization auth, DPB0158Req req, ReqHeader header) {

		DPB0158Resp resp = new DPB0158Resp();

		try {

			String id = req.getId();
			Long websiteId = Long.valueOf(id);
			Optional<DgrWebsite> websiteOptional = getDgrWebsiteDao().findById(websiteId);
			if (websiteOptional.isEmpty()) {
				throw TsmpDpAaRtnCode._1532.throwing();
			}

			DgrWebsite website = websiteOptional.orElse(null);

			String name = website.getWebsiteName();
			String status = website.getWebsiteStatus();
			String remark = website.getRemark();
			ArrayList<DPB0158ItemReq> websiteList = new ArrayList<DPB0158ItemReq>();

			List<DgrWebsiteDetail> websiteDetailList = getDgrWebsiteDetailDao().findByDgrWebsiteId(websiteId);
			websiteDetailList.forEach(e -> {
				Integer probability = e.getProbability();
				String url = e.getUrl();

				DPB0158ItemReq item = new DPB0158ItemReq();
				item.setUrl(url);
				item.setProbability(probability);

				websiteList.add(item);
			});

			resp.setWebsiteName(name);
			resp.setWebsiteStatus(status);
			resp.setRemark(remark);
			resp.setWebSiteList(websiteList);
			resp.setAuth(website.getAuth());
			resp.setSqlInjection(website.getSqlInjection());
			resp.setTraffic(website.getTraffic());
			resp.setXss(website.getXss());
			resp.setXxe(website.getXxe());
			resp.setTps(website.getTps());
			resp.setIgnoreApi(website.getIgnoreApi());
			resp.setShowLog(website.getShowLog());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrWebsiteDao getDgrWebsiteDao() {
		return dgrWebsiteDao;
	}

	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return dgrWebsiteDetailDao;
	}
}
