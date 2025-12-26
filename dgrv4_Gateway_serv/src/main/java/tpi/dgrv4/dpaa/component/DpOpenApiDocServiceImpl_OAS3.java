package tpi.dgrv4.dpaa.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpMethod;
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
 * 文件規格為 OAS 3.0
 */

@Service(value = "dpOpenApiDocServiceImpl_OAS3")
public class DpOpenApiDocServiceImpl_OAS3 implements DpOpenApiDocServiceIfs {
	
	@Override
	public AA0315Resp parseData(AA0315Req req, AA0315Resp resp, String fileData, String extFileName) throws Exception {
		ObjectMapper oas3ObjMapper = getObjMapper(extFileName);
		JsonNode rootNode = oas3ObjMapper.readTree(fileData);
		
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
		JsonNode oas3ServersNode = rootNode.get("servers");//root.servers
		if(oas3ServersNode != null && oas3ServersNode.isArray()) {
			for (JsonNode objNode : oas3ServersNode) {
				JsonNode urlNode = objNode.get("url");//root.servers.url
				isHttps = updateUrlNode(req, urlNode, isHttps, urlList, schemeList);
			}
		}
		
		String oas3Protocol = createProtocol(isHttps, schemeList);
		
		/*
		 * host: Host(IP:Port),取自 root.servers.url。依陣列順序取出第一個 url 中的 host 路徑。
		 * ex: [{"url": "https://a.b.c/v2"}, {"url": "https://d.e.f/v1"}]，則填入 "a.b.c"	
		 */
		String oas3Host = createHost(urlList);
 
		/*
		 * basePath: 來源URL的基本路徑,取自 root.servers.url。依陣列順序取出第一個 url 中的 basePath 路徑，如果沒有 basePath 則填入空字串
		 * ex: [{"url": "https://a.b.c"}, {"url": "https://d.e.f/v1"}]，則填入 "/"
		 * ex: [{"url": "https://a.b.c/v1"}, {"url": "https://d.e.f/v2"}]，則填入 "/v1"
		 */	
		String oas3BasePath = createBasePath(urlList);
		
		//moduleSrc: 模組建立來源,填入固定字串"3"，表示為 OAS 3.0 規格	
		String moduleSrc = "3";
		
		//moduleName: 模組名稱,root.info.title，若未出現此欄位則 throw 1350 ([info.title] 為必填欄位)。	
		Module m = chechNeededData(rootNode);
		
		resp.setProtocol(oas3Protocol);
		resp.setHost(oas3Host);
		resp.setBasePath(oas3BasePath);
		resp.setModuleSrc(moduleSrc);
		resp.setModuleName(m.moduleName);
		resp.setModuleVersion(m.moduleVersion);
		
		List<AA0315Item> itemList = getAA0315ItemList(rootNode, oas3Protocol, oas3Host, oas3BasePath);
		resp.setOpenApiList(itemList);
	
		return resp;
	}
	
	private Module chechNeededData(JsonNode rootNode) {
		Module m = new Module();
		JsonNode oas3InfoNode = rootNode.get("info");//root.info
		if(oas3InfoNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info");
		}
		JsonNode titleNode = oas3InfoNode.get("title");//root.info.title
		if(titleNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info.title");
		}
		m.moduleName = titleNode.asText();
		
		//moduleVersion: 模組版本,root.info.version，若未出現此欄位，則 throw 1350 ([info.version] 為必填欄位)。		
		JsonNode versionNode = oas3InfoNode.get("version");//root.info.version
		if(versionNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info.version");
		}
		m.moduleVersion = versionNode.asText();
		return m;
	}

	private static class Module{
		public String moduleName;
		public String moduleVersion;
	}

	private String createBasePath(List<String> urlList) {
		String oas3BasePath = "";
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
				oas3BasePath = url.substring(index3);
			}else {
				oas3BasePath = "/";
			}
		}
		return oas3BasePath;
	}

	private String createHost(List<String> urlList) {
		String oas3Host = "";
		if(urlList != null && !urlList.isEmpty()) {
			String url = urlList.get(0);
			int index1 = url.indexOf("://");
			int index2 = index1 + 3;
			int index3 = url.indexOf("/", index2);// 第1個"/"
			
			if(index3 > -1) {
				oas3Host = url.substring(index2, index3);
			}else {
				oas3Host = url.substring(index2);
			}
		}
		return oas3Host;
	}

	private String createProtocol(boolean isHttps, List<String> schemeList) {
		String oas3Protocol = "";
		if(isHttps) {
			oas3Protocol = "https://";
		}else {
			if(schemeList != null && !schemeList.isEmpty()) {
				oas3Protocol = schemeList.get(0);
			}
		}
		
		if(StringUtils.hasText(oas3Protocol)==false) {
			oas3Protocol = "https://";
		}
		
		if ("http".equals(oas3Protocol)) {
			oas3Protocol = "http://";
		}
		return oas3Protocol;
	}

	private boolean updateUrlNode(AA0315Req req, JsonNode urlNode, boolean isHttps, List<String> urlList, List<String> schemeList) {
		if(urlNode != null) {
			String url = urlNode.asText();
			int oas3_index = url.indexOf("://");
			String oas3Scheme = "";
			
			//若沒有提供hotst主機名稱，可由前端傳入再取得。
			if (oas3_index <= -1) {
				url = req.getOptionHost() + url;
				oas3_index = url.indexOf("://");
			}
			
			if(oas3_index > -1) {
				oas3Scheme = url.substring(0, oas3_index+3);
			}else {
				throw TsmpDpAaRtnCode._1424.throwing();
			}
			
			if("https://".equals(oas3Scheme)) {
				isHttps = true;
			}
			urlList.add(url);
			schemeList.add(oas3Scheme);
		}
		return isHttps;
	}

	private ObjectMapper getObjMapper(String extFileName) {
		ObjectMapper oas3ObjMapper = new ObjectMapper();
		if ("json".equalsIgnoreCase(extFileName)) {
			oas3ObjMapper = new ObjectMapper();
		} else {
			oas3ObjMapper = new ObjectMapper(new YAMLFactory());
		}
		return oas3ObjMapper;
	}


	public List<AA0315Item> getAA0315ItemList(JsonNode rootNode, String protocol, String oas3_host, String basePath) {
		List<AA0315Item> openApiList = new ArrayList<>();
		
		//openApiList: API清單,root.paths，若未傳入此欄位，則 throw 1350 ([paths] 為必填欄位)。需封裝成 AA0315Item 後填入		
		JsonNode oas3PathsNode = rootNode.get("paths");//root.paths
		if(oas3PathsNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("paths");
		}

		Iterator<String> pathIter = oas3PathsNode.fieldNames();
		while (pathIter.hasNext()) {//多個Path
			AA0315Item oas3Item = new AA0315Item();
			
			//path: 來源URL,root.paths.{path}，若值非以 "/" 開頭則 throw 1352 ([path] 格式不正確)
			String path = pathIter.next();			
			if(StringUtils.hasText(path)==false || !path.startsWith("/")) {
				throw TsmpDpAaRtnCode._1352.throwing("path");
			}
			JsonNode pathNode = oas3PathsNode.get(path);//root.paths.{path}
			
			/* 
			 * summary: API名稱,以 root.paths.{path}.summary 為主，
			 * 若沒有值，則取每個 method 中第一個有值的 summary (root.paths.{path}.{method}.summary)，
			 * 若都沒有 summary，則填入與 rearPath 相同的值				
			 */
			JsonNode summaryNode = pathNode.get("summary");//root.paths.{path}.summary
			String summary = "";
			if(summaryNode != null) {
				summary = summaryNode.asText();
			}
			
			//rearPath: 來源URL的最尾路徑,root.paths.{path}，截取最後一個"/"後的內容，ex: path 為 "/A/B/C" 則此欄位填入 "C"
			String[] arr = path.split("/");
			String rearPath = arr[arr.length - 1];
			
			
			/*
			 * apiDesc: API說明,以 root.paths.{path}.description 為主，
			 * 若沒有值，則取每個 method 中第一個有值的 description (root.paths.{path}.{method}.description)，
			 * 若都沒有 description，則填入空白字串
			 */
			JsonNode descriptionNode = pathNode.get("description");//root.paths.{path}.description
			String description = "";
			if(descriptionNode != null) {
				description = descriptionNode.asText();
			}
			
			Iterator<String> methodIter = pathNode.fieldNames();
			List<String> methodList = new ArrayList<>();
			List<String> consumesList = new ArrayList<>();
			List<String> producesList = new ArrayList<>();
			while (methodIter.hasNext()) {  //多個method
				String method = methodIter.next();
				String upperMethod = method.toUpperCase();	// 20210113; 需轉成大寫
				if (HttpMethod.valueOf(upperMethod) == null) {
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
					summary = updateSummary(summary, methodNode);
					
					/*
					 * apiDesc: API說明,以 root.paths.{path}.description 為主，
					 * 若沒有值，則取每個 method 中第一個有值的 description (root.paths.{path}.{method}.description)，
					 * 若都沒有 description，則填入空白字串
					 */
					description = updateDescription(description, methodNode);
					
					/*
					 * consumes: Http Consumes,整理 root.paths.{path}.{method}.requestBody.content 中的每個 MIME type 為不重複的 List。
					 * ex: {"content": {"application/json": {...}, "application/xml": {...}}}，則此欄位填入 ["application/json", "application/xml"]
					 */
					addConsumeList(consumesList, methodNode);
					
					/*
					 * produces: Http Produces,整理 root.paths.{path}.{method}.responses.{statusCode}.content 中的每個 MIME type 為不重複的 List。
					 * ex: {"content": {"application/json": {...}, "application/xml": {...}}}，則此欄位填入 ["application/json", "application/xml"]				
					 */
					addProducesList(producesList, methodNode);
				}
			}
			
			if(StringUtils.hasText(summary)==false) {//若都沒有 summary，則填入與 rearPath 相同的值
				summary = rearPath;
			}
			
			//srcUrl: 完整的來源URL,protocol + host + basePath + path，若 protocol 為空值，則預設以"https://"代替。ex:"https://host/basePath/path
			String srcUrl = getSrcUrl(protocol, oas3_host, basePath, path);
			
			//methods: Http Method,root.paths.{path}.{method}，需整理出不重複值的List，若有重複或是無元素，則 throw 1352 ([methods] 格式不正確)				
			checkMethodList(methodList);
			
			//headers: Http Header,暫不解析
			List<String> oas3HeadersList = new ArrayList<>();
			
			//params: Http Parameter,暫不解析
			List<String> oas3ParamsList = new ArrayList<>();
			
			oas3Item.setSummary(summary);
			oas3Item.setRearPath(rearPath);
			oas3Item.setPath(path);
			oas3Item.setSrcUrl(srcUrl);
			oas3Item.setApiDesc(ServiceUtil.nvl(description));
			oas3Item.setSrcUrl(srcUrl);
			oas3Item.setMethods(methodList);
			oas3Item.setHeaders(oas3HeadersList);
			oas3Item.setParams(oas3ParamsList);
			oas3Item.setConsumes(consumesList);
			oas3Item.setProduces(producesList);
			
			openApiList.add(oas3Item);
		}
		return openApiList;
	}

	private void checkMethodList(List<String> methodList) {
		if(methodList == null || methodList.isEmpty()) {
			throw TsmpDpAaRtnCode._1352.throwing("methods");
		}
		Set<String> methodSet = new HashSet<>(methodList);//List轉成Set
		if(methodList.size() != methodSet.size()) {//數目不同,表示有重複
			throw TsmpDpAaRtnCode._1352.throwing("methods");
		}
	}

	private String getSrcUrl(String protocol, String oas3_host, String basePath, String path) {
		String srcUrl = "";
		if("/".equals(basePath)) {
			srcUrl = protocol + oas3_host + path;
		}else {
			srcUrl = protocol + oas3_host + basePath + path;
		}
		return srcUrl;
	}

	private void addProducesList(List<String> producesList, JsonNode methodNode) {
		JsonNode oas3ResponsesNode = methodNode.get("responses");//root.paths.{path}.{method}.responses
		Iterator<String> statusCodeIter = oas3ResponsesNode.fieldNames();
		while (statusCodeIter.hasNext()) {//多個 statusCode
			String statusCode = statusCodeIter.next();
			JsonNode statusCodeNode = oas3ResponsesNode.get(statusCode);//root.paths.{path}.{method}.responses.{statusCode}
			JsonNode oas3ContentNode = statusCodeNode.get("content");//root.paths.{path}.{method}.responses.{statusCode}.content
			if (oas3ContentNode != null && !oas3ContentNode.isNull()) {
				Iterator<String> mimeTypeIter = oas3ContentNode.fieldNames();
				while (mimeTypeIter.hasNext()) {//多個mimeType
					String oas3_mimeType = mimeTypeIter.next();//root.paths.{path}.{method}.responses.{statusCode}.content.{mimeType}
					if(StringUtils.hasText(oas3_mimeType) && !producesList.contains(oas3_mimeType)) {
						producesList.add(oas3_mimeType);
					}
				}
			}
		}
	}

	private void addConsumeList(List<String> consumesList, JsonNode methodNode) {
		JsonNode requestBodyNode = methodNode.get("requestBody");//root.paths.{path}.{method}.requestBody
		if(requestBodyNode != null) {
			JsonNode contentNode = requestBodyNode.get("content");//root.paths.{path}.{method}.requestBody.content
			Iterator<String> mimeTypeIter = contentNode.fieldNames();
			while (mimeTypeIter.hasNext()) {//多個mimeType
				String mimeType = mimeTypeIter.next();
				if(StringUtils.hasText(mimeType) && !consumesList.contains(mimeType)) {
					consumesList.add(mimeType);
				}
			}
		}
	}

	private String updateDescription(String description, JsonNode methodNode) {
		JsonNode methodDescriptionNode = methodNode.get("description");//root.paths.{path}.{method}.description
		if(methodDescriptionNode != null) {
			String methodDescription = methodDescriptionNode.asText();
			if(StringUtils.hasText(description)==false && StringUtils.hasText(methodDescription)) {
				description = methodDescription;
			}
		}
		return description;
	}

	private String updateSummary(String summary, JsonNode methodNode) {
		JsonNode methodSummaryNode = methodNode.get("summary");//root.paths.{path}.{method}.summary
		if(methodSummaryNode != null) {
			String methodSummary = methodSummaryNode.asText();						
			if(StringUtils.hasText(summary)==false && StringUtils.hasText(methodSummary)) {
				summary = methodSummary;
			}
		}
		return summary;
	}
}
