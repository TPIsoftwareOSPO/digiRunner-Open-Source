package tpi.dgrv4.gateway.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.AcCusIdPLoginUrl;

@Service
public class AcCusIdPLoginUrlService {

	private DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao;

	@Autowired
	public AcCusIdPLoginUrlService(DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao) {
		super();
		this.dgrAcIdpInfoCusDao = dgrAcIdpInfoCusDao;
	}

	public List<AcCusIdPLoginUrl> getCusLoginUrl() {

		try {

			List<DgrAcIdpInfoCus> list = getDgrAcIdpInfoCusDao()
					.findByCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc("Y");

			Stream<AcCusIdPLoginUrl> stream = list.stream()
					.map(cus -> new AcCusIdPLoginUrl(cus.getAcIdpInfoCusName(), cus.getCusLoginUrl()));

			return stream.toList();

		} catch (Exception e) {
			// 處理其他未預期的異常
			TPILogger.tl.error("An unexpected error occurred during get CUS AC Login URL");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return Collections.emptyList();
	}

	protected DgrAcIdpInfoCusDao getDgrAcIdpInfoCusDao() {
		return this.dgrAcIdpInfoCusDao;
	}

}
