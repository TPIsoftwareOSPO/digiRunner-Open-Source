package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA1213Req;
import tpi.dgrv4.dpaa.vo.AA1213Resp;
import tpi.dgrv4.dpaa.vo.AA1213RespItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireUrlStatusInfosPacket;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;

@RequiredArgsConstructor
@Service
@Getter(AccessLevel.PROTECTED)
public class AA1213Service {
	private final TsmpApiCacheProxy tsmpApiCacheProxy;
	private final DgrcRoutingHelper dgrcRoutingHelper;
	
	//reference GatewayFilter.fetchUriHistoryList StringBuilder init
	private String urlStatusInitVal = GatewayFilter.getFetchUriHistoryListInitValue().toString();

	public AA1213Resp queryApiAbnormal(TsmpAuthorization authorization, AA1213Req req) {
		
		try {
			AA1213Resp resp = new AA1213Resp();
			TPILogger.lc.send(new RequireUrlStatusInfosPacket());
			waitRequireUrlStatusInfosPacket();
			Object urlStatusInfos = TPILogger.lc.paramObj.get("urlStatusInfos");
			List<UrlStatusPacket> urlStatusList = getUrlStatusInfos(urlStatusInfos);
			List<AA1213RespItem> dataList = new ArrayList<>();
			for(UrlStatusPacket urlStatusVo : urlStatusList) {
				String[] arrApiLog = urlStatusVo.getApiLogs().split("ms\n");
				
				for(int i=0 ; i < arrApiLog.length; i++) {
					String strApiLog = null;
					try {
						strApiLog = arrApiLog[i];
						//first row
						if(i == 0) {
							//Filter useless information EX:----------------
							//過濾無用資訊 EX:--------------
							int startIndex = strApiLog.indexOf("[");
							if(startIndex > -1) {
								strApiLog = strApiLog.substring(startIndex);
							}else {
								continue;
							}
						}
						
						String[] arrApiLogRow = strApiLog.split(",");
						if(arrApiLogRow.length == 3) {
							String strUri = arrApiLogRow[0].strip().replace("\n", "");
							//filter only uri
							strUri = strUri.substring(strUri.indexOf("(") + 1, strUri.length() - 1);
							
							//get tsmpApi info
							TsmpApi tsmpApiVo = this.getTsmpApi(strUri);
							if(tsmpApiVo == null) {
								TPILogger.tl.info("uri="+strUri+", tsmp_api not found");
							}
							
							//get resp
							AA1213RespItem respItemVo = getRespItem(req, urlStatusVo.getName(), arrApiLogRow, tsmpApiVo);
							if(respItemVo != null) {
								dataList.add(respItemVo);
							}
						}else {
							TPILogger.tl.info("arrApiLogRow.length=" + arrApiLogRow.length);
							TPILogger.tl.info("strApiLogRow=" + String.join(",", arrApiLogRow));
						}
					} catch (Exception e) {
						TPILogger.tl.error("strApiLog="+strApiLog);
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
					}
				}
			}
			if(dataList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			resp.setDataList(dataList);
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
	}
	
	private TsmpApi getTsmpApi(String strUri) {
		String[] arrUri = strUri.split("/");
		String moudleName = null;
		String apiId = null;
		TsmpApi tsmpApiVo = null;
		if("tsmpc".equals(arrUri[1].toLowerCase()) || "tsmpg".equals(arrUri[1].toLowerCase())) {
			moudleName = arrUri[2];
			apiId = arrUri[3];
		}else {
			TsmpApiReg apiReg = getDgrcRoutingHelper().calculateRoute("/dgrc"+strUri);
			if(apiReg != null) {
				moudleName = apiReg.getModuleName();
				apiId = apiReg.getApiKey();
			}else {
				moudleName = "/"+arrUri[1];
				apiId = strUri;
			}
		}
		TsmpApiId idVo = new TsmpApiId();
		idVo.setApiKey(apiId);
		idVo.setModuleName(moudleName);
		tsmpApiVo = this.getTsmpApiCacheProxy().findById(idVo).orElse(null);
		
		return tsmpApiVo;
	}
	
	private AA1213RespItem getRespItem(AA1213Req req, String nodeName, String[] arrApiLogRow, TsmpApi tsmpApiVo) {
		String strUri = arrApiLogRow[0].strip();
		Integer statusCode = Integer.valueOf(arrApiLogRow[1].strip());
		Integer elapsedTime = Integer.valueOf(arrApiLogRow[2].strip());
		AA1213RespItem itemVo = new AA1213RespItem();
		
		boolean isAbnormalData = false;
		//http status abnormal
		if(statusCode >= 400 || statusCode < 100) {
			isAbnormalData = true;
		}else {
			//http status normal, but elapsed time over setting value
			if(req.getAbnormalElapsedTime().intValue() > -1) {
				if(elapsedTime.intValue() > req.getAbnormalElapsedTime().intValue()) {
					isAbnormalData = true;
				}
			}
		}
		
		//set up respItem
		if(isAbnormalData) {
			List<String> labelList = new ArrayList<>();
			if(tsmpApiVo != null) {
				if(StringUtils.hasText(tsmpApiVo.getLabel1())) {
					labelList.add(tsmpApiVo.getLabel1());
				}
				if(StringUtils.hasText(tsmpApiVo.getLabel2())) {
					labelList.add(tsmpApiVo.getLabel2());
				}
				if(StringUtils.hasText(tsmpApiVo.getLabel3())) {
					labelList.add(tsmpApiVo.getLabel3());
				}
				if(StringUtils.hasText(tsmpApiVo.getLabel4())) {
					labelList.add(tsmpApiVo.getLabel4());
				}
				if(StringUtils.hasText(tsmpApiVo.getLabel5())) {
					labelList.add(tsmpApiVo.getLabel5());
				}
				itemVo.setApiName(tsmpApiVo.getApiName()).setLabelList(labelList);
			}
			itemVo.setNodeName(nodeName)
			.setElapsedTime(elapsedTime)
			.setStatusCode(statusCode)
			.setUri(strUri);
			return itemVo;
		}else {
			return null;
		}
	}
	
	private List<UrlStatusPacket> getUrlStatusInfos(Object infos) {
		// Check if it is null
		if (infos == null) {
			return Collections.emptyList();
		}

		// Check if it is a Collection
		if (!(infos instanceof Collection<?>)) {
			return Collections.emptyList();
		}

		Collection<?> collection = (Collection<?>) infos;

		// Check if the collection is empty
		if (collection.isEmpty()) {
			return Collections.emptyList();
		}

		// Check the first element type
		Object firstElement = collection.iterator().next();
		if (!(firstElement instanceof UrlStatusPacket)) {
			return Collections.emptyList();
		}

		// Convert and sort
		Collection<UrlStatusPacket> packets = (Collection<UrlStatusPacket>) collection;
		return packets.stream().filter(Objects::nonNull) // Filter out null elements
				.sorted(Comparator.comparing(UrlStatusPacket::getName, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
	}
	
	private void waitRequireUrlStatusInfosPacket() {
		synchronized (RequireUrlStatusInfosPacket.waitKey) {
			try {
				RequireUrlStatusInfosPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
	}
	
}
