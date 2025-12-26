package tpi.dgrv4.dpaa.service;						
						
import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0411Req;
import tpi.dgrv4.dpaa.vo.AA0411Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpDc;
import tpi.dgrv4.entity.entity.jpql.TsmpDcNode;
import tpi.dgrv4.entity.entity.jpql.TsmpNode;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpDcDao;
import tpi.dgrv4.entity.repository.TsmpDcNodeDao;
import tpi.dgrv4.entity.repository.TsmpNodeDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;						
						
@Service						
public class AA0411Service {						
						
	private TsmpUserDao tsmpUserDao; 	
	private TsmpDcDao tsmpDcDao; 			
	private TsmpNodeDao tsmpNodeDao; 	
	private TsmpDcNodeDao tsmpDcNodeDao; 	
	private DgrAcIdpUserDao dgrAcIdpUserDao; 	
	private SeqStoreService seqStoreService; 			
 				
	private TPILogger logger = TPILogger.tl; 					
	
	@Autowired
	public AA0411Service(TsmpUserDao tsmpUserDao, TsmpDcDao tsmpDcDao, TsmpNodeDao tsmpNodeDao,
			TsmpDcNodeDao tsmpDcNodeDao, DgrAcIdpUserDao dgrAcIdpUserDao, SeqStoreService seqStoreService) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpDcDao = tsmpDcDao;
		this.tsmpNodeDao = tsmpNodeDao;
		this.tsmpDcNodeDao = tsmpDcNodeDao;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.seqStoreService = seqStoreService;
	}

	public AA0411Resp addDC(TsmpAuthorization auth, AA0411Req req) {	
		AA0411Resp resp = new AA0411Resp();				
		try {				
			//check param
			checkParam(auth, req);
			
			//建立DC
			TsmpDc tsmpDc = createDcData(auth, req);
			resp.setDcId(tsmpDc.getDcId());
			
		} catch (TsmpDpAaException e) {				
			throw e;			
		} catch (Exception e) {				
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}				
		return resp;				
	}			
	
	@Transactional
	public TsmpDc createDcData(TsmpAuthorization auth, AA0411Req req) {
		TsmpDc tsmpDc = new TsmpDc();
		Long seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_DC_PK);
		tsmpDc.setDcId(seq);
		tsmpDc.setDcCode(req.getDcCode());
		tsmpDc.setDcMemo(req.getDcMemo());
		tsmpDc.setActive(false);
		tsmpDc.setCreateTime(DateTimeUtil.now());
		tsmpDc.setCreateUser(auth.getUserName());
		tsmpDc = getTsmpDcDao().saveAndFlush(tsmpDc);
		
		//若有傳入 啟用此容器的節點名稱 則每一個節點都要再寫入 TSMP_DC_NODE				
		List<String> nodeList = req.getNodeList();
		if(nodeList != null && !nodeList.isEmpty()) {
			for (String node : nodeList) {
				TsmpDcNode tsmpDcNode = new TsmpDcNode();
				tsmpDcNode.setNode(node);
				tsmpDcNode.setDcId(tsmpDc.getDcId());
				tsmpDcNode.setNodeTaskId(null);
				tsmpDcNode = getTsmpDcNodeDao().saveAndFlush(tsmpDcNode);
			}
		}
		return tsmpDc;
	}
	
	private void checkParam(TsmpAuthorization auth, AA0411Req req) {
		String dcCode = req.getDcCode();
		List<String> nodeList = req.getNodeList();
		
		// chk param
		
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		
		//使用者不存在
		checkUserExists(userNameForQuery, idPType);
		
		//檢查 部署容器代碼 是否在 TSMP_DC 中重複, 否則 throw 1284。("[{{dcCode}}] 不得重複")	
		TsmpDc tsmpDc = getTsmpDcDao().findFirstByDcCode(dcCode);//部署容器代碼
		if(tsmpDc != null) {
			throw TsmpDpAaRtnCode._1284.throwing("dcCode");
		}
		
		if(nodeList != null && !nodeList.isEmpty()) {
			//檢查傳入的 啟用此容器的節點名稱 彼此是否重複, 否則throw 1284。[{{nodeList}}] 不得重複			
			Set<String> nodeListSet = new HashSet<String>(nodeList);//List轉成Set
			if(nodeList.size() != nodeListSet.size()) {//數目不同,表示node有重複
				throw TsmpDpAaRtnCode._1284.throwing("nodeList");
			}
			
			//檢查傳入的 啟用此容器的節點名稱 是否存在 TSMP_NODE 中, 否則 throw 1354。[{{nodeList}}] 不存在: {{1}}
			for (String node : nodeList) {
				TsmpNode tsmpNode = getTsmpNodeDao().findFirstByNode(node);
				if(tsmpNode == null) {
					throw TsmpDpAaRtnCode._1354.throwing("nodeList", node);
				}
			}
		}
	}
	
	protected void checkUserExists(String userNameForQuery, String idPType) {
		if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				//Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery + ", idp_type: " + idPType);
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
		} else {//以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			//Table 查不到 user
			TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
			if (tsmpUser == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
		}
	}
						
	protected SeqStoreService getSeqStoreService() {					
		return this.seqStoreService;				
	}					
	
	protected TsmpUserDao getTsmpUserDao() {					
		return this.tsmpUserDao;				
	}					
	
	protected TsmpDcDao getTsmpDcDao() {					
		return this.tsmpDcDao;				
	}					
	
	protected TsmpNodeDao getTsmpNodeDao() {					
		return this.tsmpNodeDao;				
	}
	
	protected TsmpDcNodeDao getTsmpDcNodeDao() {					
		return this.tsmpDcNodeDao;				
	}		
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}						
						