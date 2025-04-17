package tpi.dgrv4.gateway.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPVgroupService;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupApiItem;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupItem;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupResp;

/**
 * 模擬 GTW IdP 流程中的 User 同意畫面, 顯示 Client 的 vgroup 清單
 * @author Mini
 */
@RestController
public class GtwIdPMockJSConsentUiController {
	
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private GtwIdPVgroupService gtwIdPVgroupService;

	@Autowired
	public GtwIdPMockJSConsentUiController(TsmpClientVgroupDao tsmpClientVgroupDao, TsmpVgroupDao tsmpVgroupDao,
			GtwIdPVgroupService gtwIdPVgroupService) {
		super();
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.gtwIdPVgroupService = gtwIdPVgroupService;
	}

	@GetMapping(value = "/dgrv4/mockac/gtwidp/{idPType}/consentui")
	public ResponseEntity<?> getGtwConsent(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) throws IOException {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		try {
			String reqUri = httpReq.getRequestURI();
			String state = httpReq.getParameter("state");
			String clientId = httpReq.getParameter("client_id");
			String userName = httpReq.getParameter("username");
			String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");

			// 取得client 的 vGroup
			String dgrVGroupScopeStr = "";
			GtwIdPVgroupResp gtwIdPVgroupResp = gtwIdPVgroupService.getVgroupList(httpResp, idPType, clientId, dgrClientRedirectUri, reqUri);
			List<GtwIdPVgroupItem> gtwIdPVgroupItemList = gtwIdPVgroupResp.getVgroupDataList();
			for (GtwIdPVgroupItem gtwIdPVgroupItem : gtwIdPVgroupItemList) {
				List<GtwIdPVgroupApiItem> gtwIdPVgroupApiItemList = gtwIdPVgroupItem.getApiDataList();
				for (GtwIdPVgroupApiItem gtwIdPVgroupApiItem : gtwIdPVgroupApiItemList) {
					String groupId = gtwIdPVgroupApiItem.getGroupId();
					dgrVGroupScopeStr += groupId + " ";
				}
			}
			dgrVGroupScopeStr = dgrVGroupScopeStr.trim();
			
			String dgrApproveUrl = "https://localhost:18080/dgrv4/ssotoken/gtwidp/" + idPType + "/approve";
			String redirectUrl = String.format(
					"%s" 
					+ "?username=%s" 
					+ "&scope=%s" 
					+ "&redirect_uri=%s" 
					+ "&state=%s",
					dgrApproveUrl, 
					IdPHelper.getUrlEncode(userName), 
					IdPHelper.getUrlEncode(dgrVGroupScopeStr), 
					IdPHelper.getUrlEncode(dgrClientRedirectUri), 
					IdPHelper.getUrlEncode(state)
			);
			
			TPILogger.tl.debug("Redirect to URL【dgR Approve URL】: " + redirectUrl);
			httpResp.sendRedirect(redirectUrl);
			
			return null;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
	
	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}
}
