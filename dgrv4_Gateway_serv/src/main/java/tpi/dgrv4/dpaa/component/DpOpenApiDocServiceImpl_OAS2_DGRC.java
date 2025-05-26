package tpi.dgrv4.dpaa.component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0315Item;
import tpi.dgrv4.dpaa.vo.AA0315Req;
import tpi.dgrv4.dpaa.vo.AA0315Resp;

/**
 * 文件規格為 OAS 2.0
 */

@Service(value = "dpOpenApiDocServiceImpl_OAS2_DGRC")
public class DpOpenApiDocServiceImpl_OAS2_DGRC implements DpOpenApiDocServiceIfs {
	
	public List<AA0315Item> getAA0315ItemList(JsonNode rootNode, String protocol, String host, String basePath) {
		HashMap<String, AA0315Item> dgrcHM = new HashMap<>();
		
		//openApiList: API清單,root.paths，若未傳入此欄位，則 throw 1350 ([paths] 為必填欄位)。需封裝成 AA0315Item 後填入
		JsonNode pathsNode = rootNode.get("paths");//root.paths
		if(pathsNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("paths");
		}
		
		Iterator<String> pathIter = pathsNode.fieldNames();	
		
		while (pathIter.hasNext()) {//多個Path
			AA0315Item item = new AA0315Item();
			
			//path: 來源URL,root.paths.{path}，若值非以 "/" 開頭則 throw 1352 ([path] 格式不正確)
			String path = pathIter.next();	
			
			if (!StringUtils.hasLength(path) || !path.startsWith("/")) {
				throw TsmpDpAaRtnCode._1352.throwing("path");
			}
			
			//rearPath: 來源URL的最尾路徑,root.paths.{path}，截取path的內容，ex: path 為 "/A/B/C" 則此欄位填入 "/A/B/C"
			//rearPath: 來源URL的最尾路徑,root.paths.{path}，截取path的內容，ex: path 為 "/A/B/C/{id}/" 則此欄位填入 "/A/B/C/{id}"
			String rearPath = "";
//			if (path.endsWith("/")) {
//				rearPath = path.substring(0, path.length() - 1);
//			}else {
				rearPath = path;
//			}
			
			// 將{xx} or {id}轉換成{p}
			rearPath = ServiceUtil.replaceParameterToP(rearPath);
			
			List<String> dgrcMethodList = null;
			List<String> dgrcConsumesList = null;
			List<String> dgrcProducesList = null;
			String dgrcSummary = null;
			String dgrcDescription = null;
			
			// 判斷是否已經存在的path資料
			if (dgrcHM.containsKey(rearPath)) {
				item = dgrcHM.get(rearPath);
				
				dgrcMethodList = item.getMethods();
				dgrcConsumesList = item.getConsumes();
				dgrcProducesList = item.getProduces();
				dgrcSummary = item.getSummary();
				dgrcDescription = item.getApiDesc();
			}else {
				dgrcHM.put(rearPath, item);
				
				dgrcMethodList = new ArrayList<>();
				dgrcConsumesList = new ArrayList<>();
				dgrcProducesList = new ArrayList<>();
				dgrcSummary  = "";
				dgrcDescription = "";
				
				// 取出path的第一個節點路徑當作module的分類
				if (path.split("/").length >= 3) {
					int index = path.indexOf("/", 1);
					item.setModuleName(path.substring(0, index));
				} else {
					item.setModuleName(path);
				}
				
				// 將{xx} or {id}轉換成{p}
				String moduleName = ServiceUtil.replaceParameterToP(item.getModuleName());
				item.setModuleName(moduleName);
			}
			
			JsonNode pathNode = pathsNode.get(path);//root.paths.{path}
			Iterator<String> methodIter = pathNode.fieldNames();
			
			while (methodIter.hasNext()) {//多個method
				//methods: Http Method,root.paths.{path}.{method}，需整理出不重複值的List，若有重複或是無元素，則 throw 1352 ([methods] 格式不正確)
				String method = methodIter.next();
				if(StringUtils.hasLength(method)) {
					if(dgrcMethodList.contains(method)) {//有重複
						throw TsmpDpAaRtnCode._1352.throwing("methods");
					}else {
						if (httpMethodType.contains(method.toUpperCase())) {
							dgrcMethodList.add(method);
						}
					}
				}
				
				JsonNode methodNode = pathNode.get(method);//root.paths.{path}.{method}
				if(methodNode != null) {
					/*
					 * summary: API名稱,root.paths.{path}.{method}.summary，在每個 method 中取第一個有值的 summary，
					 * 若都沒有 summary，則填入與 rearPath 相同的值
					 */
					JsonNode summaryNode = methodNode.get("summary");//root.paths.{path}.{method}.summary
					if(summaryNode != null) {
						String summaryTemp = summaryNode.asText();
						if (StringUtils.hasLength(dgrcSummary) && StringUtils.hasLength(summaryTemp)) {
							dgrcSummary = dgrcSummary + "," + summaryTemp;
						}
						if(!StringUtils.hasLength(dgrcSummary) && StringUtils.hasLength(summaryTemp)) {
							dgrcSummary = summaryTemp;
						}
					}
					
					/*
					 * apiDesc: API說明,root.paths.{path}.{method}.description，在每個 method 中取第一個有值的 description，
					 * 若都沒有 description，則填入空白字串
					 */
					JsonNode descriptionNode = methodNode.get("description");//root.paths.{path}.{method}.description
					if(descriptionNode != null) {
						String descriptionTemp = descriptionNode.asText();
						if (StringUtils.hasLength(dgrcDescription) && StringUtils.hasLength(descriptionTemp)) {
							dgrcDescription = dgrcDescription + "," + descriptionTemp;
						}
						if(!StringUtils.hasLength(dgrcDescription) && StringUtils.hasLength(descriptionTemp)) {
							dgrcDescription = descriptionTemp;
						}						
					}
					
					//consumes: Http Consumes,root.paths.{path}.{method}.consumes，把每個 method 中的 consumes 值(字串陣列)，整理成一個不重複值的List
					JsonNode consumesNode = methodNode.get("consumes");//root.paths.{path}.{method}.consumes
					if(consumesNode != null && consumesNode.isArray()) {//是否為陣列
						for (JsonNode objNode : consumesNode) {
							String consume = objNode.asText();
							if(StringUtils.hasLength(consume) && !dgrcConsumesList.contains(consume)) {
								dgrcConsumesList.add(consume);
							}
						}
					}
					
					//produces: Http Produces,root.paths.{path}.{method}.produces，把每個 method 中的 produces 值(字串陣列)，整理成一個不重複值的List
					JsonNode producesNode = methodNode.get("produces");//root.paths.{path}.{method}.produces
					if(producesNode != null && producesNode.isArray()) {//是否為陣列
						for (JsonNode objNode : producesNode) {
							String produce = objNode.asText();
							if(StringUtils.hasLength(produce) && !dgrcProducesList.contains(produce)) {
								dgrcProducesList.add(produce);
							}
						}
					}
				}
			}
			
//			if (path.endsWith("/")) {
//				path = path.substring(0, path.length() - 1);
//			}
			
			// 將{xx} or {id}轉換成{p}
			path = ServiceUtil.replaceParameterToP(path);
			
			//srcUrl: 完整的來源URL,protocol + host + basePath + path，若 protocol 為空值，則預設以"https://"代替。ex:"https://host/basePath/path
			String srcUrl = "";
			if("/".equals(basePath)) {
				srcUrl = protocol + host + path;
			}else {
				srcUrl = protocol + host + basePath + path;
			}

			
			//methods: Http Method,root.paths.{path}.{method}，需整理出不重複值的List，若有重複或是無元素，則 throw 1352 ([methods] 格式不正確)
			if(dgrcMethodList == null || dgrcMethodList.isEmpty()) {
				throw TsmpDpAaRtnCode._1352.throwing("methods");
			}
 
			//headers: Http Header,暫不解析
			List<String> headersList = new ArrayList<>();
			
			//params: Http Parameter,暫不解析
			List<String> paramsList = new ArrayList<>();
			
			item.setSummary(dgrcSummary);
			item.setRearPath(rearPath);
			item.setPath(path);
			item.setSrcUrl(srcUrl);
			item.setApiDesc(ServiceUtil.nvl(dgrcDescription));
			item.setSrcUrl(srcUrl);
			item.setMethods(dgrcMethodList);
			item.setHeaders(headersList);
			item.setParams(paramsList);
			item.setConsumes(dgrcConsumesList);
			item.setProduces(dgrcProducesList);
			
		}
		
		List<AA0315Item> list = dgrcHM.values().stream().sorted(Comparator.comparing(AA0315Item::getPath))
				.collect(Collectors.toCollection(ArrayList::new));
		
		return list;
	}
	
	@Override
	public AA0315Resp parseData(AA0315Req req ,AA0315Resp oas2DgrcResp, String fileData, String extFileName) throws Exception {
		ObjectMapper objMapper = null;
		if ("json".equalsIgnoreCase(extFileName)) {
			objMapper = new ObjectMapper();
		} else {
			objMapper = new ObjectMapper(new YAMLFactory());
		}
		JsonNode rootNode = objMapper.readTree(fileData);
		
		/* 
		 * protocol: 協定方式,root.schemes，此欄位為字串陣列(Optional)，若元素中有出現"https"則優先回傳，否則依陣列順序取出(只取一個)。
		 * ex: ["https", "http"]，則回傳"https://"
		 * ex: ["http"]，則回傳"http://"
		 */
		JsonNode schemesNode = rootNode.get("schemes");//root.schemes
		
		boolean isHttps = false;
		List<String> protocolTempList = new ArrayList<>();
		if(schemesNode != null && schemesNode.isArray()) {
			for (JsonNode objNode : schemesNode) {
				String scheme = objNode.asText();
				if(StringUtils.hasLength(scheme)) {
					if(scheme.indexOf("https") > -1) {
						isHttps = true;
					}
					protocolTempList.add(scheme);
				}
			}
		}
		String protocol = "";
		if(isHttps) {
			protocol = "https://";
		}else {
			if(protocolTempList != null && !protocolTempList.isEmpty()) {
				protocol = protocolTempList.get(0);
			}
		}
 
		if(!StringUtils.hasLength(protocol)) {
			protocol = "https://";
		}
		
		if ("http".equals(protocol)) {
			protocol = "http://";
		}
		
		String host = "";
		//host: Host(IP:Port),root.host，若未出現此欄位則 throw 1424
		JsonNode hostNode = rootNode.get("host");//root.host
		if (hostNode != null) {
			host = hostNode.asText();
		}
		
		
		//basePath: 來源URL的基本路徑,root.basePath，若未出現此欄位則預設填入空字串,若有值，但非以"/"開頭，則 throw 1352 ([basePath] 格式不正確)
		JsonNode basePathNode = rootNode.get("basePath");//root.basePath
		String basePath = "";
		if (basePathNode != null) {
			basePath = basePathNode.asText();
			boolean flag = basePath.startsWith("/");
			if(!flag) {
				basePath = "";
			}
		}
		
		//moduleSrc: 模組建立來源,填入固定字串"2"，表示為 OAS 2.0 規格
		String moduleSrc = "2";
		
		//moduleName: 模組名稱,root.info.title，若未出現此欄位則 throw 1350 ([info.title] 為必填欄位)。	
		JsonNode oas2DgrcInfoNode = rootNode.get("info");//root.info
		if(oas2DgrcInfoNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info");
		}
		JsonNode oas2DgrcTitleNode = oas2DgrcInfoNode.get("title");//root.info.title
		if(oas2DgrcTitleNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info.title");
		}
		String moduleName = oas2DgrcTitleNode.asText();
		
		//moduleVersion: 模組版本,root.info.version，若未出現此欄位，則 throw 1350 ([info.version] 為必填欄位)。
		JsonNode versionNode = oas2DgrcInfoNode.get("version");//root.info.version
		String moduleVersion = null;
		if(versionNode == null) {
			moduleVersion = "v1";
		}else {
			moduleVersion = versionNode.asText();
		}
		
		
		oas2DgrcResp.setProtocol(protocol);
		oas2DgrcResp.setHost(host);
		oas2DgrcResp.setBasePath(basePath);
		oas2DgrcResp.setModuleSrc(moduleSrc);
		oas2DgrcResp.setModuleName(moduleName);
		oas2DgrcResp.setModuleVersion(moduleVersion);
			
		
		List<AA0315Item> itemList = getAA0315ItemList(rootNode, protocol, host, basePath);
		oas2DgrcResp.setOpenApiList(itemList);
	
		return oas2DgrcResp;
	}
}
