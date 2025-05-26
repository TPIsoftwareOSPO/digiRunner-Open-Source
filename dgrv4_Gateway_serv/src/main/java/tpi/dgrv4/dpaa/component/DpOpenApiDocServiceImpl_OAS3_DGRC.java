package tpi.dgrv4.dpaa.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.SafeHttpMethod;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0315Item;
import tpi.dgrv4.dpaa.vo.AA0315Req;
import tpi.dgrv4.dpaa.vo.AA0315Resp;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件規格為 OAS 3.0
 */

@Service(value = "dpOpenApiDocServiceImpl_OAS3_DGRC")
public class DpOpenApiDocServiceImpl_OAS3_DGRC implements DpOpenApiDocServiceIfs {
	
	public List<AA0315Item> getAA0315ItemList(JsonNode rootNode, String protocol, String host, String basePath) {
		HashMap<String, AA0315Item> aa0315ItemHM = new HashMap<>();
		
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
			if(StringUtils.hasLength(path) == false || !path.startsWith("/")) {
				throw TsmpDpAaRtnCode._1352.throwing("path");
			}
			JsonNode pathNode = pathsNode.get(path);//root.paths.{path}
			
			
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
			
			List<String> methodList = null;
			List<String> consumesList = null;
			List<String> producesList = null;
			String summary = null;
			String description = null;
			boolean hasValueSummary = false;
			boolean hasValueDescription = false;
			
			// 判斷是否已經存在的path資料
			if (aa0315ItemHM.containsKey(rearPath)) {
				item = aa0315ItemHM.get(rearPath);
				
				methodList = item.getMethods();
				consumesList = item.getConsumes();
				producesList = item.getProduces();
				summary = item.getSummary();
				description = item.getApiDesc();
			}else {
				aa0315ItemHM.put(rearPath, item);
			
				methodList = new ArrayList<>();
				consumesList = new ArrayList<>();
				producesList = new ArrayList<>();
				summary  = "";
				description = "";
				
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
			
			/* 
			 * summary: API名稱,以 root.paths.{path}.summary 為主，
			 * 若沒有值，則取每個 method 中第一個有值的 summary (root.paths.{path}.{method}.summary)，
			 * 若都沒有 summary，則填入與 rearPath 相同的值				
			 */
			JsonNode summaryNode = pathNode.get("summary");//root.paths.{path}.summary

			if(summaryNode != null) {
				if (StringUtils.hasLength(summary)) {
					summary = summary + "," +summaryNode.asText();	
				}
				if (!StringUtils.hasLength(summary)) {
					summary = summaryNode.asText();
				}
				hasValueSummary = true;
			}
			
			
			/*
			 * apiDesc: API說明,以 root.paths.{path}.description 為主，
			 * 若沒有值，則取每個 method 中第一個有值的 description (root.paths.{path}.{method}.description)，
			 * 若都沒有 description，則填入空白字串
			 */
			JsonNode descriptionNode = pathNode.get("description");//root.paths.{path}.description

			if(descriptionNode != null) {
				if (StringUtils.hasLength(description)) {
					description = description + "," +descriptionNode.asText();	
				}
				if (!StringUtils.hasLength(description)) {
					description = descriptionNode.asText();	
				}
				hasValueDescription = true;
			}
			
			Iterator<String> methodIter = pathNode.fieldNames();
			
			while (methodIter.hasNext()) {//多個method
				String method = methodIter.next();
				String upperMethod = method.toUpperCase();	// 20210113; 需轉成大寫
				if (SafeHttpMethod.resolve(upperMethod) == null) {
					continue;
				}
				if (httpMethodType.contains(method.toUpperCase())) {
					methodList.add(upperMethod);
				}
				
				JsonNode methodNode = pathNode.get(method);//root.paths.{path}.{method}
				if(methodNode != null) {
					/* 
					 * summary: API名稱,以 root.paths.{path}.summary 為主，
					 * 若沒有值，則取每個 method 中第一個有值的 summary (root.paths.{path}.{method}.summary)，
					 * 若都沒有 summary，則填入與 rearPath 相同的值				
					 */
					if (hasValueSummary == false) {
						JsonNode methodSummaryNode = methodNode.get("summary");//root.paths.{path}.{method}.summary
						if(methodSummaryNode != null) {
							String methodSummary = methodSummaryNode.asText();						
							if(StringUtils.hasLength(summary) && StringUtils.hasLength(methodSummary)) {
								summary = summary + "," +methodSummary;
							}
							if(!StringUtils.hasLength(summary) && StringUtils.hasLength(methodSummary)) {
								summary = methodSummary;
							}							
						}
					}
					
					
					/*
					 * apiDesc: API說明,以 root.paths.{path}.description 為主，
					 * 若沒有值，則取每個 method 中第一個有值的 description (root.paths.{path}.{method}.description)，
					 * 若都沒有 description，則填入空白字串
					 */
					if (hasValueDescription == false ) {
						JsonNode methodDescriptionNode = methodNode.get("description");//root.paths.{path}.{method}.description
						if(methodDescriptionNode != null) {
							String methodDescription = methodDescriptionNode.asText();
							if(StringUtils.hasLength(description) && StringUtils.hasLength(methodDescription)) {
								description = description + "," + methodDescription;
							}
							if(!StringUtils.hasLength(description) && StringUtils.hasLength(methodDescription)) {
								description = methodDescription;
							}							
						}
					}
					
					
					/*
					 * consumes: Http Consumes,整理 root.paths.{path}.{method}.requestBody.content 中的每個 MIME type 為不重複的 List。
					 * ex: {"content": {"application/json": {...}, "application/xml": {...}}}，則此欄位填入 ["application/json", "application/xml"]
					 */
					JsonNode requestBodyNode = methodNode.get("requestBody");//root.paths.{path}.{method}.requestBody
					if(requestBodyNode != null) {
						JsonNode contentNode = requestBodyNode.get("content");//root.paths.{path}.{method}.requestBody.content
						Iterator<String> mimeTypeIter = contentNode.fieldNames();
						while (mimeTypeIter.hasNext()) {//多個mimeType
							String mimeType = mimeTypeIter.next();
							if(StringUtils.hasLength(mimeType) && !consumesList.contains(mimeType)) {
								consumesList.add(mimeType);
							}
						}
					}
					
					/*
					 * produces: Http Produces,整理 root.paths.{path}.{method}.responses.{statusCode}.content 中的每個 MIME type 為不重複的 List。
					 * ex: {"content": {"application/json": {...}, "application/xml": {...}}}，則此欄位填入 ["application/json", "application/xml"]				
					 */
					JsonNode responsesNode = methodNode.get("responses");//root.paths.{path}.{method}.responses
					Iterator<String> statusCodeIter = responsesNode.fieldNames();
					while (statusCodeIter.hasNext()) {//多個 statusCode
						String statusCode = statusCodeIter.next();
						JsonNode statusCodeNode = responsesNode.get(statusCode);//root.paths.{path}.{method}.responses.{statusCode}
						JsonNode contentNode = statusCodeNode.get("content");//root.paths.{path}.{method}.responses.{statusCode}.content
						if (contentNode != null && !contentNode.isNull()) {
							Iterator<String> mimeTypeIter = contentNode.fieldNames();
							while (mimeTypeIter.hasNext()) {//多個mimeType
								String mimeType = mimeTypeIter.next();//root.paths.{path}.{method}.responses.{statusCode}.content.{mimeType}
								if(!StringUtils.isEmpty(mimeType) && !producesList.contains(mimeType)) {
									producesList.add(mimeType);
								}
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
			if(methodList == null || methodList.isEmpty()) {
				throw TsmpDpAaRtnCode._1352.throwing("methods");
			}
			Set<String> methodSet = new HashSet<>(methodList);//List轉成Set
			if(methodList.size() != methodSet.size()) {//數目不同,表示有重複
				throw TsmpDpAaRtnCode._1352.throwing("methods");
			}
			
			//headers: Http Header,暫不解析
			List<String> headersList = new ArrayList<>();
			
			//params: Http Parameter,暫不解析
			List<String> paramsList = new ArrayList<>();
			
			item.setSummary(summary);
			item.setRearPath(rearPath);
			item.setPath(path);
			item.setSrcUrl(srcUrl);
			item.setApiDesc(ServiceUtil.nvl(description));
			item.setSrcUrl(srcUrl);
			item.setMethods(methodList);
			item.setHeaders(headersList);
			item.setParams(paramsList);
			item.setConsumes(consumesList);
			item.setProduces(producesList);
			
			
		}
		
		List<AA0315Item> list = aa0315ItemHM.values().stream().sorted(Comparator.comparing(AA0315Item::getPath))
				.collect(Collectors.toCollection(ArrayList::new));
		
		return list;
	}
	
	@Override
	public AA0315Resp parseData(AA0315Req req, AA0315Resp resp, String fileData, String extFileName) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		if ("json".equalsIgnoreCase(extFileName)) {
			objMapper = new ObjectMapper();
		} else {
			objMapper = new ObjectMapper(new YAMLFactory());
		}
		JsonNode rootNode = objMapper.readTree(fileData);
		
		/*
		 * protocol: 協定方式,從 root.servers.url 取出。
		 * 若每個 server 的 url 只出現 相對路徑(沒有 scheme) 則 throw 1424 (未指定主機位址)。
		 * 若 url 有 https 則優先填入，否則依 server 的順序取得協定
		 * ex: [{"url": "http://..."}, {"url": "https://"}]，則回傳 "https://"
		 * ex: [{"url": "ws://..."}, {"url": "http://"}]，則回傳 "ws://"				
		 */
 
		boolean isHttps = false;
		List<String> urlList = new ArrayList<>();
		List<String> schemeList = new ArrayList<>();
		JsonNode serversNode = rootNode.get("servers");//root.servers
		if(serversNode != null) {
			if(serversNode.isArray()) {
				for (JsonNode objNode : serversNode) {
					JsonNode urlNode = objNode.get("url");//root.servers.url
					if(urlNode != null) {
						String url = urlNode.asText();
						int index = url.indexOf("://");
						String scheme = "";
						
						//若沒有提供hotst主機名稱，可由前端傳入再取得。
						if (index <= -1) {
							url = req.getOptionHost() + url;
							index = url.indexOf("://");
						}
						
						if(index > -1) {
							scheme = url.substring(0, index+3);
						}else {
							continue ;
						}
						
						if("https://".equals(scheme)) {
							isHttps = true;
						}
						urlList.add(url);
						schemeList.add(scheme);
					}
				}
			}
		}
		
		String protocol = "";
		if(isHttps) {
			protocol = "https://";
		}else {
			if(schemeList != null && !schemeList.isEmpty()) {
				protocol = schemeList.get(0);
			}
		}
		
		if(!StringUtils.hasLength(protocol)) {
			protocol = "https://";
		}
		
		if ("http".equals(protocol)) {
			protocol = "http://";
		}
		
		/*
		 * host: Host(IP:Port),取自 root.servers.url。依陣列順序取出第一個 url 中的 host 路徑。
		 * ex: [{"url": "https://a.b.c/v2"}, {"url": "https://d.e.f/v1"}]，則填入 "a.b.c"	
		 */
		String host = "";
		if(urlList != null && !urlList.isEmpty()) {
			String url = urlList.get(0);
			int index1 = url.indexOf("://");
			int index2 = index1 + 3;
			int index3 = url.indexOf("/", index2);// 第1個"/"
			
			if(index3 > -1) {
				host = url.substring(index2, index3);
			}else {
				host = url.substring(index2);
			}
		}
 
		/*
		 * basePath: 來源URL的基本路徑,取自 root.servers.url。依陣列順序取出第一個 url 中的 basePath 路徑，如果沒有 basePath 則填入空字串
		 * ex: [{"url": "https://a.b.c"}, {"url": "https://d.e.f/v1"}]，則填入 "/"
		 * ex: [{"url": "https://a.b.c/v1"}, {"url": "https://d.e.f/v2"}]，則填入 "/v1"
		 */	
		String basePath = "";
		if(urlList != null && !urlList.isEmpty()) {
			String url = urlList.get(0);
			int index1 = url.indexOf("://");
			int index2 = index1 + 3;
			int index3 = url.indexOf("/", index2);// 第1個"/"
			/* 20210113; 改切 host 之後的全部
			int index4 = url.indexOf("/", index3+1);// 第2個"/"
			
			if(index3 > -1 && index4 > -1) {
				basePath = url.substring(index3, index4);
			}else */ if(index3 > -1) {
				basePath = url.substring(index3);
			}else {
				basePath = "/";
			}
		}
		
		//moduleSrc: 模組建立來源,填入固定字串"3"，表示為 OAS 3.0 規格	
		String moduleSrc = "3";
		
		//moduleName: 模組名稱,root.info.title，若未出現此欄位則 throw 1350 ([info.title] 為必填欄位)。	
		JsonNode infoNode = rootNode.get("info");//root.info
		if(infoNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info");
		}
		JsonNode titleNode = infoNode.get("title");//root.info.title
		if(titleNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info.title");
		}
		String moduleName = titleNode.asText();
		
		//moduleVersion: 模組版本,root.info.version，若未出現此欄位，則 throw 1350 ([info.version] 為必填欄位)。		
		JsonNode versionNode = infoNode.get("version");//root.info.version
		String moduleVersion = null;
		if(versionNode == null) {
			//throw TsmpDpAaRtnCode._1350.throwing("info.version");
			moduleVersion = "v1";
		}else {
			moduleVersion = versionNode.asText();
		}
		
		resp.setProtocol(protocol);
		resp.setHost(host);
		resp.setBasePath(basePath);
		resp.setModuleSrc(moduleSrc);
		resp.setModuleName(moduleName);
		resp.setModuleVersion(moduleVersion);
		
		List<AA0315Item> itemList = getAA0315ItemList(rootNode, protocol, host, basePath);
		resp.setOpenApiList(itemList);
	
		return resp;
	}
}
