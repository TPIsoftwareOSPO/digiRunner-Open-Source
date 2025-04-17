package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0225Req;
import tpi.dgrv4.dpaa.vo.AA0225Resp;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0225Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupApiDao tsmpGroupAPiDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	
	@Autowired
	public AA0225Service(TsmpGroupDao tsmpGroupDao, TsmpGroupApiDao tsmpGroupAPiDao,
			TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao, TsmpVgroupDao tsmpVgroupDao,
			TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao, TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao,
			TsmpVgroupGroupDao tsmpVgroupGroupDao, TsmpClientVgroupDao tsmpClientVgroupDao) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupAPiDao = tsmpGroupAPiDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpVgroupAuthoritiesMapDao = tsmpVgroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
	}

	@Transactional
	public AA0225Resp deleteVGroup(AA0225Req req) {
		AA0225Resp resp = new AA0225Resp();
		try {
			checkParams(req);
			updateTables(req);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();		// 1287:刪除失敗
			
		}
		return resp;
	}

	/**
	 * 1.虛擬群組ID 搜尋不到資料時(TSMP_VGROUP), throw 1298:查無資料
	 * <br>
	 * 2. 若有用戶端已使用該虛擬群組(TSMP_CLIENT_VGROUP), 則 throw 1403:無法刪除，請解除用戶端的虛擬授權設定。
	 * 
	 * @param auth
	 * @param req
	 * @throws Exception
	 */
	private void checkParams(AA0225Req req) throws Exception {
		// 虛擬群組ID 搜尋不到資料時(TSMP_VGROUP), throw 1298:查無資料
		String vgroupId = req.getVgroupId();
		Optional<TsmpVgroup> optVgroup = getTsmpVgroupDao().findById(vgroupId);
		if(!optVgroup.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
			
		// 2. 若有用戶端已使用該虛擬群組(TSMP_CLIENT_VGROUP), 則 throw 1403:無法刪除，請解除用戶端的虛擬授權設定。
		List<TsmpClientVgroup> cvgList = getTsmpClientVgroupDao().findByVgroupId(vgroupId);
		if(cvgList != null && cvgList.size() >0) {
			throw TsmpDpAaRtnCode._1403.throwing();
		}
		
	}
	
	/**
	 * 1. 找出此虛擬群組下有哪些群組(TSMP_VGROUP_GROUP)
	 * 2. 刪除那些群組下的API(TSMP_GROUP_API)後，再刪除群組核身對應資料(TSMP_GROUP_AUTHORITIES_MAP)及群組本身(TSMP_GROUP)		
	 * 3. 刪除虛擬群組與群組的對應(TSMP_VGROUP_GROUP)、虛擬群組核身對應(TSMP_VGROUP_AUTHORITIES_MAP)及此虛擬群組(TSMP_VGROUP)
	 * 
	 * @param req		
	 */
	private void updateTables(AA0225Req req) {
		//如果有被client端使用就不可以被刪除, 相關的group(vgroup有選API所產生)有Tsmp_Client_group,OAUTH_CLIENT_DETAILS.scope不在這邊刪除.
		//必須在用戶端那邊移除虛擬群組維護
		String vgroupId = nvl(req.getVgroupId());
		List<TsmpVgroupGroup> vggList = getTsmpVgroupGroupDao().findByVgroupId(vgroupId);
		
		if(vggList != null) {
			vggList.forEach((vgg)->{
				String groupId = nvl(vgg.getGroupId());
				//刪除TSMP_GROUP_API
				List<TsmpGroupApi> gaList = getTsmpGroupApiDao().findByGroupId(groupId);
				if(gaList != null) {
					getTsmpGroupApiDao().deleteAll(gaList);
				}
				//刪除TSMP_GROUP_AUTHORITIES_MAP
				List<TsmpGroupAuthoritiesMap> gamList = getTsmpGroupAuthoritiesMapDao().findByGroupId(groupId);
				if(gamList != null) {
					getTsmpGroupAuthoritiesMapDao().deleteAll(gamList);
				}
				//刪除TSMP_GROUP
				Optional<TsmpGroup> optGroup = getTsmpGroupDao().findById(groupId);
				if(optGroup.isPresent()) {
					getTsmpGroupDao().delete(optGroup.get());
				}else {
					logger.debug("groupId =" + groupId + "找不到");
					throw TsmpDpAaRtnCode._1287.throwing();
				}
			});
		}
		
		//刪除TSMP_VGROUP_GROUP
		getTsmpVgroupGroupDao().deleteAll(vggList);
		
		//TSMP_VGROUP_AUTHORITIES_MAP
		List<TsmpVgroupAuthoritiesMap> vgamList = getTsmpVgroupAuthoritiesMapDao().findByVgroupId(vgroupId);
		getTsmpVgroupAuthoritiesMapDao().deleteAll(vgamList);
		
		//刪除TSMP_VGROUP
		getTsmpVgroupDao().deleteById(vgroupId);
		
	}
	
	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupAPiDao;
	}
	
	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return this.tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return this.tsmpVgroupDao;
	}
	
	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return this.tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return this.tsmpGroupAuthoritiesDao;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return this.tsmpVgroupGroupDao;
	}
	
	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return this.tsmpClientVgroupDao;
	}

}
