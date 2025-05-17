package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0323Flow;
import tpi.dgrv4.dpaa.vo.AA0323Req;
import tpi.dgrv4.dpaa.vo.AA0323Resp;
import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;
import tpi.dgrv4.entity.repository.DgrComposerFlowDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;

@Service
public class AA0323Service {

	private TPILogger logger = TPILogger.tl;

	private DgrComposerFlowDao dgrComposerFlowDao;
	private CApiKeyService capiKeyService;

	@Autowired
	public AA0323Service(DgrComposerFlowDao dgrComposerFlowDao, CApiKeyService capiKeyService) {
		super();
		this.dgrComposerFlowDao = dgrComposerFlowDao;
		this.capiKeyService = capiKeyService;
	}

	public AA0323Resp exportComposerFlow(AA0323Req req, ReqHeader reqHeader,
			HttpHeaders headers) {
		AA0323Resp resp = new AA0323Resp();

		// 驗證CApiKey
		getCapiKeyService().verifyCApiKey(headers, true, true);

		try {

			List<String> flowData = new ArrayList<String>();
			List<AA0323Flow> flows = req.getFlows();
			if (flows==null || flows.isEmpty()) {
				List<DgrComposerFlow> list = getDgrComposerFlowDao().findAll();
				list.stream().forEach(c -> flowData.add(c.getFlowDataAsString()));
			} else {
				for (AA0323Flow aa0323Flow : flows) {
					Optional<DgrComposerFlow> opt_dgrComposerFlow = getDgrComposerFlowDao()
							.findByModuleNameAndApiId(aa0323Flow.getModuleName(), aa0323Flow.getApiName());
					if (opt_dgrComposerFlow.isPresent()) {
						DgrComposerFlow dgrComposerFlow = opt_dgrComposerFlow.get();
						flowData.add(dgrComposerFlow.getFlowDataAsString());
					}
				}
			}
			resp.setFlowData(flowData);
			
			String composerID = req.getComposerID();
			this.logger.debugDelay2sec("["+composerID+"] flows has been refreshed");
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected DgrComposerFlowDao getDgrComposerFlowDao() {
		return dgrComposerFlowDao;
	}
	
	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}

}
