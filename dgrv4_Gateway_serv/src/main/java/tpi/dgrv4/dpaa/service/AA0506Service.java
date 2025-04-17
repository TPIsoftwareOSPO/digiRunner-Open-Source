package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.ReportType;
import tpi.dgrv4.dpaa.vo.AA0506Req;
import tpi.dgrv4.dpaa.vo.AA0506Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0506Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpReportUrlDao tsmpReportUrlDao;
	private TsmpSettingDao tsmpSettingDao;

	@Autowired
	public AA0506Service(TsmpReportUrlDao tsmpReportUrlDao, TsmpSettingDao tsmpSettingDao) {
		super();
		this.tsmpReportUrlDao = tsmpReportUrlDao;
		this.tsmpSettingDao = tsmpSettingDao;
	}

	public AA0506Resp queryReportUrls(AA0506Req req) {
		AA0506Resp resp = new AA0506Resp();
		try {
			String reportId = req.getReportID();
			String timeRange = req.getTimeRange();

			/*
			 * 1.查詢TSMP_REPORT_URL資料表 條件 → WHERE REPORT_ID LIKE AA0506Req.reportID AND
			 * TIME_RANGE = AA0506Req.timeRange
			 */
			TsmpReportUrl reportUrl = getTsmpReportUrlDao().findByTimeRangeAndReportId(timeRange, reportId);

			if (reportUrl == null) {
				throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
			}
			String id = reportUrl.getReportId();
			String url = reportUrl.getReportUrl();
			String reportType = "";
			if(null!=url) {
				URI uri = new URI(url);
				
				if(null == uri.getHost()) {
					reportType = ReportType.SYSTEM_REPORT;
				}else if(url.indexOf("/dgrv4/cus") > -1){
					reportType = ReportType.EMBEDDED_LINKS;
					url = uri.getPath();
				}else {
					reportType = ReportType.OUTBOUND_LINKS;
				}
			}
			

			/*
			 * 3.取得Kibana主機的Port，查詢TSMP_SETTING資料表 條件 → WHERE ID = "TSMP_REPORT_ADDRESS"
			 */
			Optional<TsmpSetting> optSetting = getTsmpSettingDao().findById("TSMP_REPORT_ADDRESS");

			// 4.將步驟3取得的TSMP_SETTING.VALUE放進AA0506Resp.rpport，欄位對應參考AA0506Resp
			if (!optSetting.isPresent()) {
				logger.debug("getTsmpSettingDao().findById(TSMP_REPORT_ADDRESS) not found !");
				throw TsmpDpAaRtnCode._1297.throwing();
			}

			TsmpSetting setting = optSetting.get();
			String value = setting.getValue();
			Integer rpport = null;
			rpport = Integer.parseInt(value);
		
			resp.setRpport(rpport);
			resp.setReportID(nvl(id));
			resp.setReportUrl(nvl(url));
			resp.setReportType(reportType);
			/*
			 * 4.取得Kibana 的路由，查詢TSMP_SETTING資料表 條件 → WHERE ID = "KIBANA_REPORTURL_PREFIX"
			 */
			Optional<TsmpSetting> optSetting2 = getTsmpSettingDao().findById(TsmpSettingDao.Key.KIBANA_REPORTURL_PREFIX);

			// 6.將步驟5取得的TSMP_SETTING.VALUE放進AA0506Resp.rpport，欄位對應參考AA0506Resp
			if (!optSetting2.isPresent()) {
				logger.debug("getTsmpSettingDao().findById(KIBANA_REPORTURL_PREFIX) not found !");
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			resp.setRpContentPath(optSetting2.get().getValue());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return this.tsmpReportUrlDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}

}
