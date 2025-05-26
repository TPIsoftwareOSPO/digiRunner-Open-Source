package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0174Req;
import tpi.dgrv4.dpaa.vo.DPB0174Resp;
import tpi.dgrv4.dpaa.vo.DPB0174RespItem;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0174Service {

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0174Service(DgrWebSocketMappingDao dgrWebSocketMappingDao, ServiceConfig serviceConfig) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0174Resp queryWsList(TsmpAuthorization auth, DPB0174Req req) {
		DPB0174Resp resp = new DPB0174Resp();
		try {
				
			Long longId = StringUtils.hasText(req.getLongId()) ? Long.valueOf(req.getLongId()) : null;
			String[] keywords = ServiceUtil.getKeywords(req.getKeyword(), " ");
			List<DgrWebSocketMapping> list = getDgrWebSocketMappingDao().queryDPB0174(longId, keywords, this.getPageSize());
			if(CollectionUtils.isEmpty(list)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<DPB0174RespItem> dataList = new ArrayList<>();
			list.forEach(vo->{
				DPB0174RespItem itemVo = new DPB0174RespItem();
				itemVo.setHexId(RandomSeqLongUtil.toHexString(vo.getWsMappingId(), RandomLongTypeEnum.YYYYMMDD));
				itemVo.setLongId(vo.getWsMappingId().toString());
				itemVo.setSiteName(vo.getSiteName());
				itemVo.setTargetWs(vo.getTargetWs());
				itemVo.setChangeUser(StringUtils.hasText(vo.getUpdateUser()) ? vo.getUpdateUser() : vo.getCreateUser());
				String changeDateTime = null;
				if(vo.getUpdateDateTime() != null) {
					changeDateTime = DateTimeUtil.dateTimeToString(vo.getUpdateDateTime(), DateTimeFormatEnum.西元年月日時分秒).get();
				}else {
					changeDateTime = DateTimeUtil.dateTimeToString(vo.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分秒).get();
				}
				itemVo.setChangeDateTime(changeDateTime);
				
				dataList.add(itemVo);
			});
			
			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0174");
		return this.pageSize;
	}

}
