package tpi.dgrv4.dpaa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.record.UrlStatusRecord;
import tpi.dgrv4.dpaa.vo.AA1212ApiThreadStatusResp;
import tpi.dgrv4.dpaa.vo.AA1212BadAttemptItemResp;
import tpi.dgrv4.dpaa.vo.AA1212BadAttemptResp;
import tpi.dgrv4.dpaa.vo.AA1212CacheResp;
import tpi.dgrv4.dpaa.vo.AA1212DbResp;
import tpi.dgrv4.dpaa.vo.AA1212FailResp;
import tpi.dgrv4.dpaa.vo.AA1212LastLoginLog;
import tpi.dgrv4.dpaa.vo.AA1212NodeInfoResp;
import tpi.dgrv4.dpaa.vo.AA1212QueueResp;
import tpi.dgrv4.dpaa.vo.AA1212RankedResp;
import tpi.dgrv4.dpaa.vo.AA1212Resp;
import tpi.dgrv4.dpaa.vo.AA1212RespItem;
import tpi.dgrv4.dpaa.vo.AA1212SuccessResp;
import tpi.dgrv4.dpaa.vo.RealtimeDashboardAllNodeVo;
import tpi.dgrv4.entity.entity.DgrAuditLogM;
import tpi.dgrv4.entity.repository.DgrAuditLogMDao;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireRealtimeDashboardInfosPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireUrlStatusInfosPacket;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.tcp.utils.packets.RealtimeDashboardPacket;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;

@RequiredArgsConstructor
@Service
@Getter(AccessLevel.PROTECTED)
public class AA1212Service {

	private int noKeeperServerNumber = 0;
	private final DgrAuditLogMDao dgrAuditLogMDao;
	private long startTime = System.currentTimeMillis();

	public AA1212Resp queryRealtimeDashboardData(TsmpAuthorization authorization) {
		
		try {
			AA1212Resp resp = new AA1212Resp();
			
			//last login log
			List<DgrAuditLogM> auditLogMList = getDgrAuditLogMDao().findTop3ByUserNameAndEventNoOrderByCreateDateTimeDesc(authorization.getUserName(), "login");
			List<AA1212LastLoginLog> lastLoginLogList = auditLogMList.stream().map(alm -> {
				AA1212LastLoginLog log = new AA1212LastLoginLog();
				log.setLoginDate(DateTimeUtil.dateTimeToString(alm.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分秒_2).get());
				log.setLoginIp(alm.getUserIp());
				log.setLoginStatus(alm.getParam1());
				return log;
			}).collect(Collectors.toList());		
			resp.setLastLoginLogList(lastLoginLogList);
			
			if(!authorization.getAuthorities().contains("1000") && !authorization.getAuthorities().contains("1001")) {
				return resp;
			}
			
			if (TPILogger.lc != null) {
				//init
				RealtimeDashboardAllNodeVo allNodeVo = new RealtimeDashboardAllNodeVo();
				List<AA1212RespItem> dataList = new ArrayList<>();
				
				//allClientList
				processAllClientList(dataList, allNodeVo);
				
				//realtimeDashboardInfos
				processRealtimeDashboardInfos(dataList, allNodeVo);
				
				//urlStatusInfos
				processUrlStatusInfos(dataList, allNodeVo);
		        
		        //dataList sort, getKeeperServer(Y or N)->getNodeName
				dataList = dataList.stream().sorted(Comparator.comparing(AA1212RespItem::getKeeperServer, Comparator.reverseOrder())
		        		.thenComparing(AA1212RespItem::getNodeName)).collect(Collectors.toList());
		        
		        //allNode
		        AA1212RespItem itemVo = processAllNode(allNodeVo);
		        //allNode add index 0
		        dataList.add(0, itemVo);
				
				resp.setDataList(dataList);
			}else {
				//It is printed every 30 times it is called, otherwise the keeperServer does not need to be opened.
				//每被呼叫30次才印,要不然keeperServer是可以不開的
				if(noKeeperServerNumber < Integer.MAX_VALUE && ++noKeeperServerNumber % 30 == 0) {
					TPILogger.tl.error("queryRealtimeDashboardData <Keeper Server> Lost Connection");
				}
			}
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
	}
	
	private void processAllClientList(List<AA1212RespItem> dataList, RealtimeDashboardAllNodeVo allNodeVo) {
		TPILogger.lc.send(new RequireAllClientListPacket());
		myWaitLock();
		LinkedList<ClientKeeper> clientList = (LinkedList<ClientKeeper>) TPILogger.lc.paramObj.get("allClientList");
		if(CollectionUtils.isEmpty(clientList)) {
			return;
		}
		for(ClientKeeper clientVo : clientList) {
			// Ignore custom packages
			if("1".equals(clientVo.getProjectType())) {
				continue;
			}
			//allClientList scope value
			AA1212RespItem itemVo = new AA1212RespItem();
			//nodeName, updateTime, Req/s, Resp/s
			itemVo.setNodeName(clientVo.getUsername());
			itemVo.setUpdateTime(clientVo.getUpTime());
			itemVo.setReqTps(clientVo.getApi_ReqThroughputSize() + "/s");
			itemVo.setRespTps(clientVo.getApi_RespThroughputSize() + "/s");
			
			//all node values
			//keeper server restart maybe is null
			if(clientVo.getApi_ReqThroughputSize() != null) {
				allNodeVo.setReqTps(allNodeVo.getReqTps() + Integer.parseInt(clientVo.getApi_ReqThroughputSize()));
			}
			if(clientVo.getApi_RespThroughputSize() != null) {
				allNodeVo.setRespTps(allNodeVo.getRespTps() + Integer.parseInt(clientVo.getApi_RespThroughputSize()));
			}
			
			//cache
			AA1212CacheResp cacheResp = new AA1212CacheResp();
			cacheResp.setDao(clientVo.getDaoCacheSize());
			cacheResp.setFixed(clientVo.getFixedCacheSize());
			cacheResp.setRcd(clientVo.getRcdCacheSize());
			itemVo.setCache(cacheResp);
			//queue
			AA1212QueueResp queueResp = new AA1212QueueResp();
			queueResp.setEs(clientVo.getEsQueue());
			queueResp.setRdb(clientVo.getRdbQueue());
			itemVo.setQueue(queueResp);
			//ip
			itemVo.setIp(clientVo.getIp() + ":" + clientVo.getServerPort());
			//keeperServer
			if(clientVo.getWebLocalIP().equals(clientVo.getKeeperServerIp())) {
				itemVo.setKeeperServer("Y");
			}
			
			//CpuUsage
			AA1212NodeInfoResp nodeInfoResp = new AA1212NodeInfoResp();
			nodeInfoResp.setCpuUsage(clientVo.getCpu());
			itemVo.setNodeInfo(nodeInfoResp);
			
			dataList.add(itemVo);
		}
		
	}
	
	private void processRealtimeDashboardInfos(List<AA1212RespItem> dataList, RealtimeDashboardAllNodeVo allNodeVo) {
		TPILogger.lc.send(new RequireRealtimeDashboardInfosPacket());
		Object realtimeDashboardInfos = TPILogger.lc.paramObj.get("realtimeDashboardInfos");
		List<RealtimeDashboardPacket> realtimeDashboardList = getRealtimeDashboardInfos(realtimeDashboardInfos);
		for(AA1212RespItem itemVo : dataList) {
			for(RealtimeDashboardPacket dashboardVo : realtimeDashboardList) {
				//match nodeName
				//因為itemVo會多出digiRunner.gtw.deploy.id的值, 所以用startsWith
				//Because itemVo will have an extra value of digiRunner.gtw.deploy.id, so use indexOf
				if(dashboardVo != null && itemVo.getNodeName().startsWith(dashboardVo.getName())) {
					//totalRequest
					long totalRequest = dashboardVo.getSuccess() + dashboardVo.getFail();
					itemVo.setTotalRequest(String.format("%,d", totalRequest));
					//success
					AA1212SuccessResp successResp = new AA1212SuccessResp();
					successResp.setSuccess(String.format("%,d", dashboardVo.getSuccess()));
					successResp.setTotal(String.format("%,d", totalRequest));
					BigDecimal bdTotal = new BigDecimal(totalRequest + "");
					BigDecimal bdSuccess = new BigDecimal(dashboardVo.getSuccess() + "");
					String percentage = "0.00%";
					if(totalRequest > 0 && dashboardVo.getSuccess() > 0) {
						percentage = String.format("%.2f", bdSuccess.divide(bdTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100) + "%";
					}
					successResp.setPercentage(percentage);
					itemVo.setSuccess(successResp);
					//fail
					AA1212FailResp failResp = new AA1212FailResp();
					failResp.setFail(String.format("%,d", dashboardVo.getFail()));
					failResp.setTotal(String.format("%,d", totalRequest));
					BigDecimal bdFail = new BigDecimal(dashboardVo.getFail() + "");
					percentage = "0.00%";
					if(totalRequest > 0 && dashboardVo.getFail() > 0) {
						percentage = String.format("%.2f", bdFail.divide(bdTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100) + "%";
					}
					failResp.setPercentage(percentage);
					itemVo.setFail(failResp);
					
					//bad attempt
					AA1212BadAttemptResp badAttemptResp = new AA1212BadAttemptResp();
					badAttemptResp.setCode401(String.format("%,d", dashboardVo.getBadAttempt401()));
					badAttemptResp.setCode403(String.format("%,d", dashboardVo.getBadAttempt403()));
					badAttemptResp.setOthers(String.format("%,d", dashboardVo.getBadAttemptOthers()));
					itemVo.setBadAttempt(badAttemptResp);
					
					//all node values
					allNodeVo.setTotalRequest(allNodeVo.getTotalRequest() + totalRequest);
					allNodeVo.setSuccess(allNodeVo.getSuccess() + dashboardVo.getSuccess());
					allNodeVo.setFail(allNodeVo.getFail() + dashboardVo.getFail());
					allNodeVo.setCode401(allNodeVo.getCode401() + dashboardVo.getBadAttempt401());
					allNodeVo.setCode403(allNodeVo.getCode403() + dashboardVo.getBadAttempt403());
					allNodeVo.setOthers(allNodeVo.getOthers() + dashboardVo.getBadAttemptOthers());
					
					//db
					AA1212DbResp dbResp = new AA1212DbResp();
					dbResp.setActive(dashboardVo.getDbActive());
					dbResp.setIdle(dashboardVo.getDbIdle());
					dbResp.setTotal(dashboardVo.getDbTotal());
					dbResp.setWaiting(dashboardVo.getDbWaiting());
					itemVo.setDb(dbResp);
					
					//threadStatus
					AA1212ApiThreadStatusResp threadResp = new AA1212ApiThreadStatusResp();
					threadResp.setCountryRoadActvieCount(dashboardVo.getCountryRoadActiveCount());
					threadResp.setCountryRoadPoolSize(dashboardVo.getCountryRoadPoolSize());
					threadResp.setHighwayActvieCount(dashboardVo.getHighwayActiveCount());
					threadResp.setHighwayPoolSize(dashboardVo.getHighwayPoolSize());
					itemVo.setApiThreadStatus(threadResp);
					
					//nodeInfo	
					AA1212NodeInfoResp nodeInfoResp = itemVo.getNodeInfo();
					nodeInfoResp.setCpuCore(dashboardVo.getCpuCore());
					nodeInfoResp.setMemFree(dashboardVo.getMemFree());
					nodeInfoResp.setMemMax(dashboardVo.getMemMax());
					nodeInfoResp.setMemTotal(dashboardVo.getMemTotal());
					itemVo.setNodeInfo(nodeInfoResp);
					
					break;
				}
			}
		}
	}
	
	private void processUrlStatusInfos(List<AA1212RespItem> dataList, RealtimeDashboardAllNodeVo allNodeVo) {
		TPILogger.lc.send(new RequireUrlStatusInfosPacket());
		Object urlStatusInfos = TPILogger.lc.paramObj.get("urlStatusInfos");
		List<UrlStatusPacket> urlStatusList = getUrlStatusInfos(urlStatusInfos);
		for(AA1212RespItem itemVo : dataList) {
			List<UrlStatusRecord> urlStatusRecordList = new ArrayList<>();
			List<UrlStatusRecord> above400StatusList = new ArrayList<>();
			for(UrlStatusPacket urlStatusVo : urlStatusList) {
				//因為itemVo會多出digiRunner.gtw.deploy.id的值, 所以用startsWith
				//Because itemVo will have an extra value of digiRunner.gtw.deploy.id, so use indexOf
				if(urlStatusVo != null && itemVo.getNodeName().startsWith(urlStatusVo.getName())) {
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
							if(arrApiLogRow.length >= 3) {
								String strUri = arrApiLogRow[0].strip();
								Integer statusCode = Integer.valueOf(arrApiLogRow[1].strip());
								Integer elapsedTime = Integer.valueOf(arrApiLogRow[2].replace("ms", "").strip());
								UrlStatusRecord urlStatusRecord = new UrlStatusRecord(strUri, statusCode, elapsedTime);
								urlStatusRecordList.add(urlStatusRecord);
								allNodeVo.getUrlStatusRecordList().add(urlStatusRecord);
								
								//400 above list
								if(statusCode >= 400 || statusCode < 100) {
									UrlStatusRecord above400StatusRecord = new UrlStatusRecord(strUri, statusCode, null);
									above400StatusList.add(above400StatusRecord);
									allNodeVo.getAbove400StatusSet().add(above400StatusRecord);
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
					//BadAttemptList
					List<AA1212BadAttemptItemResp> badAttemptList = above400StatusList.stream()
				            .sorted(Comparator.comparing(UrlStatusRecord::statusCode))
				            .map(recordVo -> new AA1212BadAttemptItemResp(recordVo.uri(), recordVo.statusCode()+""))
				            .collect(Collectors.toList());
					itemVo.setBadAttemptList(badAttemptList);
					
					// Get the top 5 largest elapse transactions
					// 取得 elapse 最大的前 5 筆
			        List<AA1212RankedResp> inclundeFailSlowList = urlStatusRecordList.stream()
			            .sorted(Comparator.comparing(UrlStatusRecord::elapsedTime).reversed())
			            .limit(5)
			            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
			            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
			        List<AA1212RankedResp> exclundeFailSlowList = urlStatusRecordList.stream()
				            .filter(vo->vo.statusCode() >= 200 && vo.statusCode() < 400 )
			        		.sorted(Comparator.comparing(UrlStatusRecord::elapsedTime).reversed())  // Sort from large to small
				            .limit(5)
				            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
				            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
			        itemVo.setInclundeFailSlowList(inclundeFailSlowList);
			        itemVo.setExclundeFailSlowList(exclundeFailSlowList);
			        
                    // Get the top 5 smallest elapse transactions
			        // 取得 elapse 最小的前 5 筆
			        List<AA1212RankedResp> inclundeFailFastList = urlStatusRecordList.stream()
			            .sorted(Comparator.comparing(UrlStatusRecord::elapsedTime))
			            .limit(5)
			            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
			            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
			        List<AA1212RankedResp> exclundeFailFastList = urlStatusRecordList.stream()
			        		.filter(vo->vo.statusCode() >= 200 && vo.statusCode() < 400 )
				            .sorted(Comparator.comparing(UrlStatusRecord::elapsedTime))
				            .limit(5)
				            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
				            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
			        itemVo.setInclundeFailFastList(inclundeFailFastList);
			        itemVo.setExclundeFailFastList(exclundeFailFastList);
					break;
				}
				
			}
		}
	}
	
	private AA1212RespItem processAllNode(RealtimeDashboardAllNodeVo allNodeVo) {
		//all node value to resp
		AA1212RespItem itemVo = new AA1212RespItem();
		//nodeName, updateTime, Req/s, Resp/s
		itemVo.setNodeName(allNodeVo.getNodeName());
		itemVo.setUpdateTime(DateTimeUtil.secondsToDaysHoursMinutesSeconds(System.currentTimeMillis() - this.startTime));
		itemVo.setReqTps(allNodeVo.getReqTps() + "/s");
		itemVo.setRespTps(allNodeVo.getRespTps() + "/s");
		//totalRequest
		itemVo.setTotalRequest(String.format("%,d", allNodeVo.getTotalRequest()));
		//success
		AA1212SuccessResp successResp = new AA1212SuccessResp();
		successResp.setSuccess(String.format("%,d", allNodeVo.getSuccess()));
		successResp.setTotal(String.format("%,d", allNodeVo.getTotalRequest()));
		BigDecimal bdTotal = new BigDecimal(allNodeVo.getTotalRequest());
		BigDecimal bdSuccess = new BigDecimal(allNodeVo.getSuccess());
		String percentage = "0.00%";
		if(allNodeVo.getTotalRequest() > 0 && allNodeVo.getSuccess() > 0) {
			percentage = String.format("%.2f", bdSuccess.divide(bdTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100) + "%";
		}
		successResp.setPercentage(percentage);
		itemVo.setSuccess(successResp);
		//fail
		AA1212FailResp failResp = new AA1212FailResp();
		failResp.setFail(String.format("%,d", allNodeVo.getFail()));
		failResp.setTotal(String.format("%,d", allNodeVo.getTotalRequest()));
		BigDecimal bdFail = new BigDecimal(allNodeVo.getFail());
		percentage = "0.00%";
		if(allNodeVo.getTotalRequest() > 0 && allNodeVo.getFail() > 0) {
			percentage = String.format("%.2f", bdFail.divide(bdTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100) + "%";
		}
		failResp.setPercentage(percentage);
		itemVo.setFail(failResp);
		
		//bad attempt
		AA1212BadAttemptResp badAttemptResp = new AA1212BadAttemptResp();
		badAttemptResp.setCode401(String.format("%,d", allNodeVo.getCode401()));
		badAttemptResp.setCode403(String.format("%,d", allNodeVo.getCode403()));
		badAttemptResp.setOthers(String.format("%,d", allNodeVo.getOthers()));
		itemVo.setBadAttempt(badAttemptResp);
		
		//BadAttemptList
		List<AA1212BadAttemptItemResp> badAttemptList = allNodeVo.getAbove400StatusSet().stream()
	            .sorted(Comparator.comparing(UrlStatusRecord::statusCode))
	            .map(recordVo -> new AA1212BadAttemptItemResp(recordVo.uri(), recordVo.statusCode()+""))
	            .collect(Collectors.toList());
		itemVo.setBadAttemptList(badAttemptList);
		
		// Get the top 5 largest elapse transactions
		// 取得 elapse 最大的前 5 筆
        List<AA1212RankedResp> inclundeFailSlowList = allNodeVo.getUrlStatusRecordList().stream()
	            .collect(Collectors.toMap(
		                record -> record.uri(),   // key
		                record -> record,         // value
		                (record1, record2) -> record1.elapsedTime().compareTo(record2.elapsedTime()) >= 0 ? record1 : record2 //same key save max value
		         ))
	            .values().stream()
            .sorted(Comparator.comparing(UrlStatusRecord::elapsedTime).reversed())  // Sort from large to small
            .limit(5)
            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
        List<AA1212RankedResp> exclundeFailSlowList = allNodeVo.getUrlStatusRecordList().stream()
        		.filter(vo->vo.statusCode() >= 200 && vo.statusCode() < 400 )
	            .collect(Collectors.toMap(
		                record -> record.uri(),   // key
		                record -> record,         // value
		                (record1, record2) -> record1.elapsedTime().compareTo(record2.elapsedTime()) >= 0 ? record1 : record2 //same key save max value
		         ))
	            .values().stream()
        		.sorted(Comparator.comparing(UrlStatusRecord::elapsedTime).reversed())  // Sort from large to small
	            .limit(5)
	            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
	            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
        itemVo.setInclundeFailSlowList(inclundeFailSlowList);
        itemVo.setExclundeFailSlowList(exclundeFailSlowList);
        
        // Get the top 5 smallest elapse transactions
        // 取得 elapse 最小的前 5 筆
        List<AA1212RankedResp> inclundeFailFastList = allNodeVo.getUrlStatusRecordList().stream()
	            .collect(Collectors.toMap(
		                record -> record.uri(),   // key
		                record -> record,         // value
		                (record1, record2) -> record1.elapsedTime().compareTo(record2.elapsedTime()) <= 0 ? record1 : record2 //same key save min value
		         ))
	            .values().stream()
            .sorted(Comparator.comparing(UrlStatusRecord::elapsedTime))
            .limit(5)
            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
        List<AA1212RankedResp> exclundeFailFastList = allNodeVo.getUrlStatusRecordList().stream()
        		.filter(vo->vo.statusCode() >= 200 && vo.statusCode() < 400 )
	            .collect(Collectors.toMap(
		                record -> record.uri(),   // key
		                record -> record,         // value
		                (record1, record2) -> record1.elapsedTime().compareTo(record2.elapsedTime()) <= 0 ? record1 : record2 //same key save min value
		         ))
	            .values().stream()
        		
	            .sorted(Comparator.comparing(UrlStatusRecord::elapsedTime))
	            .limit(5)
	            .map(recordVo -> new AA1212RankedResp(recordVo.uri(), recordVo.statusCode()
	            		, String.format("%,d", recordVo.elapsedTime()))).collect(Collectors.toList());
        itemVo.setInclundeFailFastList(inclundeFailFastList);
        itemVo.setExclundeFailFastList(exclundeFailFastList);
        
        return itemVo;
	}
		
	
	private List<RealtimeDashboardPacket> getRealtimeDashboardInfos(Object infos) {
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
		if (!(firstElement instanceof RealtimeDashboardPacket)) {
			return Collections.emptyList();
		}

		// Convert and sort
		Collection<RealtimeDashboardPacket> packets = (Collection<RealtimeDashboardPacket>) collection;
		return packets.stream().filter(Objects::nonNull) // Filter out null elements
				//.sorted(Comparator.comparing(RealtimeDashboardPacket::getName, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
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
				//.sorted(Comparator.comparing(UrlStatusPacket::getName, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
	}
	
	private void myWaitLock() {
		synchronized (RequireAllClientListPacket.waitKey) {
			try {
				RequireAllClientListPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
	}

}
