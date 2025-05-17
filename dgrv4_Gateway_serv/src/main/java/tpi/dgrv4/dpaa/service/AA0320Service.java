package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0320Item;
import tpi.dgrv4.dpaa.vo.AA0320Req;
import tpi.dgrv4.dpaa.vo.AA0320Resp;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0320Service {

	private TPILogger logger = TPILogger.tl;

	private Integer pageSize;

	private TsmpGroupDao tsmpGroupDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private ServiceConfig serviceConfig;

	@Autowired
	public AA0320Service(TsmpGroupDao tsmpGroupDao, TsmpVgroupDao tsmpVgroupDao, TsmpVgroupGroupDao tsmpVgroupGroupDao,
			TsmpGroupApiDao tsmpGroupApiDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.serviceConfig = serviceConfig;
	}


	public AA0320Resp queryGroupApiList(AA0320Req req) {
		AA0320Resp resp = new AA0320Resp();
		// 1296:缺少必填參數, 1297:執行錯誤, 1298:查無資料, 1354:[{{0}}] 不存在: {{1}}
		try {
			// 1. 若未傳入 AA0320Req.moduleName 或 AA0320Req.apiKey 則 throw 1296。
			if(StringUtils.isEmpty(req.getModuleName()) || StringUtils.isEmpty(req.getApiKey())) {
				throw TsmpDpAaRtnCode._1296.throwing();		//1296:缺少必填參數
			}
			String[] keywords = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String gId = nvl(req.getgId());
			String apiKey = req.getApiKey();
			String moduleName = req.getModuleName();
			
			List<TsmpGroupApi> tspmGroupApiList = getTsmpGroupApiDao().query_aa0320Service(gId, moduleName, apiKey, keywords, getPageSize());
			
			if(tspmGroupApiList == null || tspmGroupApiList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();		//1298:查無資料
			}
			
			//parse data
			List<AA0320Item> itemList = getAA0320Items(tspmGroupApiList, gId);
			resp.setApiKey(apiKey);
			resp.setModuleName(moduleName);
			resp.setDataList(itemList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();		//1297:執行錯誤
		}
		return resp;
	}
	
	
	private List<AA0320Item> getAA0320Items(List<TsmpGroupApi> tspmGroupApiList, String gId){
		List<AA0320Item> itemList = new ArrayList<AA0320Item>();
		tspmGroupApiList.forEach((ga)->{
			AA0320Item item = new AA0320Item();
			//用 TSMP_GROUP_API.group_id 關聯 TSMP_GROUP.group_id，找出群組資料，查無資料throw 1354 ([group_id] 不存在: {{1}})。					
			String gaGroupId = nvl(ga.getGroupId());
			Optional<TsmpGroup> groupOpt = getTsmpGroupDao().findById(gaGroupId);
			if(!groupOpt.isPresent()) {
				//查無資料throw 1354 ([group_id] 不存在: {{1}})。	
				throw TsmpDpAaRtnCode._1354.throwing("group_id", gaGroupId);
			}
			
			/*若 TSMP_GROUP.vgroup_flag = ""1""，則再以 group_id 查詢 TSMP_VGROUP_GROUP.group_id，
			   找出虛擬群組編號 (TSMP_VGROUP_GROUP.vgroup_id)，再以 vgroup_id 查詢 TSMP_VGROUP，找出一筆虛擬群組資料*/
			TsmpGroup group = groupOpt.get();
			String vgroupFlag = nvl(group.getVgroupFlag());
			if("1".equals(vgroupFlag)) {
				List<TsmpVgroupGroup> vggList = getTsmpVgroupGroupDao().findByGroupId(group.getGroupId());
				/*基本上 group如果vgroupFlag=1.只會對應一個vgroup 
				  同一個 group_id 在 TSMP_VGROUP_GROUP 中，應只會有一筆資料，若超過一筆則 throw 1297*/
				if(vggList != null && vggList.size() > 0) {
					TsmpVgroupGroup vgg = vggList.get(0);
					if(vgg != null) {
						if(vggList.size() > 1) {
							logger.debug("group_id = "+group.getGroupId() +" in Table(TSMP_VGROUP_GROUP) is duplicate ");
							throw TsmpDpAaRtnCode._1297.throwing();		//1297:執行錯誤
						}
						
						String vgId = nvl(vgg.getVgroupId());
						Optional<TsmpVgroup> vgroupOpt = getTsmpVgroupDao().findById(vgId);
						if(vgroupOpt.isPresent()) {
							TsmpVgroup vg = vgroupOpt.get();
							item = setAA0320Item(vg, gaGroupId);
						}
					}
				}
			}else {
				item = setAA0320Item(group, gaGroupId);
			}
			itemList.add(item);
		});
		
		return itemList;
	}
	
	private AA0320Item setAA0320Item(Object o, String gId) {
		AA0320Item item = new AA0320Item();
		if(o instanceof TsmpGroup) {
			TsmpGroup group = (TsmpGroup)o;
			item.setgId(gId);
			item.setName(nvl(group.getGroupName()));
			item.setAlias(group.getGroupAlias());
			item.setDesc(group.getGroupDesc());
			item.setV("N");
		}else if(o instanceof TsmpVgroup) {
			TsmpVgroup vgroup = (TsmpVgroup)o;
			item.setgId(gId);
			item.setName(nvl(vgroup.getVgroupName()));
			item.setAlias(vgroup.getVgroupAlias());
			item.setDesc(vgroup.getVgroupDesc());
			item.setV("Y");
			item.setvId(vgroup.getVgroupId());
		}
		return item;
	}
	
	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return this.tsmpVgroupDao;
	}
	
	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return this.tsmpVgroupGroupDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupApiDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0320");
		return this.pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
}
