package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0207GroupItem;
import tpi.dgrv4.dpaa.vo.DPB0207Req;
import tpi.dgrv4.dpaa.vo.DPB0207Resp;
import tpi.dgrv4.dpaa.vo.DPB0207RespItem;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.entity.DgrXApiKeyMap;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.repository.DgrXApiKeyDao;
import tpi.dgrv4.entity.repository.DgrXApiKeyMapDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * @author Mini <br>
 *         查詢 Client ID 的 X-Api-Key 清單
 */
@Service
public class DPB0207Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private DgrXApiKeyDao dgrXApiKeyDao;
	private DgrXApiKeyMapDao dgrXApiKeyMapDao;
	private TsmpGroupDao tsmpGroupDao;

	@Autowired
	public DPB0207Service(TsmpClientDao tsmpClientDao, DgrXApiKeyDao dgrXApiKeyDao, DgrXApiKeyMapDao dgrXApiKeyMapDao,
			TsmpGroupDao tsmpGroupDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrXApiKeyDao = dgrXApiKeyDao;
		this.dgrXApiKeyMapDao = dgrXApiKeyMapDao;
		this.tsmpGroupDao = tsmpGroupDao;
	}

	public DPB0207Resp queryXApiKeyListByClientId(TsmpAuthorization authorization, DPB0207Req req) {
		DPB0207Resp resp = new DPB0207Resp();

		try {
			String clientId = req.getClientId();
			if (!StringUtils.hasLength(clientId)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{clientId}}");
			}

			TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
			if (client == null) {
				throw TsmpDpAaRtnCode._1344.throwing();
			}

			List<DgrXApiKey> xApiKeyList = getDgrXApiKeyDao()
					.findByClientIdOrderByCreateDateTimeDescApiKeyIdDesc(clientId);
			if (xApiKeyList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<DPB0207RespItem> itemList = new ArrayList<>();

			xApiKeyList.forEach(xApiKey -> {
				Long apiKeyId = xApiKey.getApiKeyId();
				String stringId = RandomSeqLongUtil.toHexString(apiKeyId, RandomLongTypeEnum.YYYYMMDD);

				// 取得 X-Api-Key 可存取的群組資料
				List<DPB0207GroupItem> groupItemList = getGroupItemList(apiKeyId);

				DPB0207RespItem item = new DPB0207RespItem();
				item.setId(stringId);
				item.setLongId(apiKeyId + "");
				item.setClientId(xApiKey.getClientId());
				item.setApiKeyAlias(xApiKey.getApiKeyAlias());
				item.setApiKeyMask(xApiKey.getApiKeyMask());
				item.setEffectiveAt(xApiKey.getEffectiveAt() + "");
				item.setExpiredAt(xApiKey.getExpiredAt() + "");
				item.setCreateDateTime(xApiKey.getCreateDateTime().getTime() + "");
				item.setCreateUser(xApiKey.getCreateUser());
				item.setGroupDataList(groupItemList);
				itemList.add(item);
			});

			resp.setDataList(itemList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	/**
	 * 取得 X-Api-Key 可存取的群組資料
	 */
	private List<DPB0207GroupItem> getGroupItemList(long apiKeyId) {
		List<DPB0207GroupItem> groupItemList = new LinkedList<>();
		List<DgrXApiKeyMap> xApiKeyMapList = getDgrXApiKeyMapDao().findByRefApiKeyId(apiKeyId);
		for (DgrXApiKeyMap xApiKeyMap : xApiKeyMapList) {// 多筆
			String groupId = xApiKeyMap.getGroupId();
			if (StringUtils.hasLength(groupId)) {
				TsmpGroup tsmpGroup = getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(groupId, "0");
				if (tsmpGroup != null) {
					String groupName = tsmpGroup.getGroupName();
					String groupAlias = tsmpGroup.getGroupAlias();
					String groupDesc = tsmpGroup.getGroupDesc();

					DPB0207GroupItem groupItem = new DPB0207GroupItem();
					groupItemList.add(groupItem);
					groupItem.setGroupId(groupId);
					groupItem.setGroupName(groupName);
					groupItem.setGroupAlias(groupAlias);
					groupItem.setGroupDesc(groupDesc);
				}
			}
		}

		return groupItemList;
	}

	protected DgrXApiKeyDao getDgrXApiKeyDao() {
		return dgrXApiKeyDao;
	}

	protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
		return dgrXApiKeyMapDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
}
