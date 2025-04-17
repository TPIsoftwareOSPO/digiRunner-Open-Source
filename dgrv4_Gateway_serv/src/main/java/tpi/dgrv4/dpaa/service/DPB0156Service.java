package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0156Req;
import tpi.dgrv4.dpaa.vo.DPB0156Resp;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Service
public class DPB0156Service {

	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;
	private TsmpSettingDao tsmpSettingDao;
	private TsmpSettingService tsmpSettingService;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0156Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao,
			TsmpSettingDao tsmpSettingDao, TsmpSettingService tsmpSettingService) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Transactional
	public DPB0156Resp deleteWebsite(TsmpAuthorization auth, DPB0156Req req, ReqHeader header) {

		DPB0156Resp resp = new DPB0156Resp();

		try {

			Long websiteId = req.getDgrWebsiteId();
			if (websiteId == null) {
				this.logger.error("Website ID is Null !");
				throw TsmpDpAaRtnCode._1287.throwing();
			}

			Optional<DgrWebsite> website = getDgrWebsiteDao().findById(websiteId);
			if (website.isEmpty()) {
				this.logger.error("Website ID is Not Found !");
				throw TsmpDpAaRtnCode._1287.throwing();
			}

			getDgrWebsiteDao().deleteById(websiteId);
			getDgrWebsiteDetailDao().deleteByDgrWebsiteId(websiteId);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}

	protected DgrWebsiteDao getDgrWebsiteDao() {
		return dgrWebsiteDao;
	}

	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return dgrWebsiteDetailDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}


}
