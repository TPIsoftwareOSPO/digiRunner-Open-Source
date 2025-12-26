package tpi.dgrv4.dpaa.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
@Service(value = "dpOpenApiDocServiceImpl_OAS2")
public class DpOpenApiDocServiceImplOAS2 implements DpOpenApiDocServiceIfs {
	private ObjectMapper getObjMapper(String extFileName) {
		ObjectMapper oas2ObjMapper = null;
		if ("json".equalsIgnoreCase(extFileName)) {
			oas2ObjMapper = new ObjectMapper();
		} else {
			oas2ObjMapper = new ObjectMapper(new YAMLFactory());
		}
		return oas2ObjMapper;
	}
	
	private ProtocolTempList createProtocolTempList(JsonNode oas2SchemesNode){
		ProtocolTempList ptl = new ProtocolTempList();
		ptl.protocolTempList = new ArrayList<>();
		if(oas2SchemesNode != null && oas2SchemesNode.isArray()) {
			for (JsonNode objNode : oas2SchemesNode) {
				String scheme = objNode.asText();
				if(StringUtils.hasText(scheme)) {
					if(scheme.indexOf("https") > -1) {
						ptl.isHttps = true;
					}
					ptl.protocolTempList.add(scheme);
				}
			}
		}
		return ptl;
	} 
	
	private static class ProtocolTempList{
		public List<String> protocolTempList;
		public boolean isHttps = false;
	}

	private String getProtocol(ProtocolTempList ptl) {
		List<String> protocolTempList = ptl.protocolTempList;
		String protocol = "";
		if(ptl.isHttps) {
			protocol = "https://";
		}else {
			if(protocolTempList != null && !protocolTempList.isEmpty()) {
				protocol = protocolTempList.get(0);
			}
		}
 
		if(!StringUtils.hasText(protocol)) {
			protocol = "https://";
		}
		
		if ("http".equals(protocol)) {
			protocol = "http://";
		}
		return protocol;
	}
	
	@Override
	public AA0315Resp parseData(AA0315Req req ,AA0315Resp resp, String fileData, String extFileName) throws Exception {
		ObjectMapper oas2ObjMapper = getObjMapper(extFileName);
		JsonNode rootNode = oas2ObjMapper.readTree(fileData);
		
		/* 
		 * protocol: 協定方式,root.schemes，此欄位為字串陣列(Optional)，若元素中有出現"https"則優先回傳，否則依陣列順序取出(只取一個)。
		 * ex: ["https", "http"]，則回傳"https://"
		 * ex: ["http"]，則回傳"http://"
		 */
		JsonNode oas2SchemesNode = rootNode.get("schemes");//root.schemes
		
		
		ProtocolTempList ptl = createProtocolTempList(oas2SchemesNode);
		String protocol = getProtocol(ptl);
		
		String host = "";
		//host: Host(IP:Port),root.host，若未出現此欄位則 throw 1424
		JsonNode oas2HostNode = rootNode.get("host");//root.host
		if (oas2HostNode != null) {
			host = oas2HostNode.asText();
		}
		
		//basePath: 來源URL的基本路徑,root.basePath，若未出現此欄位則預設填入空字串,若有值，但非以"/"開頭，則 throw 1352 ([basePath] 格式不正確)
		JsonNode oas2BasePathNode = rootNode.get("basePath");//root.basePath
		String basePath = createBasepath(oas2BasePathNode);
		
		
		//moduleSrc: 模組建立來源,填入固定字串"2"，表示為 OAS 2.0 規格
		String moduleSrc = "2";
		
		//moduleName: 模組名稱,root.info.title，若未出現此欄位則 throw 1350 ([info.title] 為必填欄位)。
		Module m = chechNeededData(rootNode);
		resp.setProtocol(protocol);
		resp.setHost(host);
		resp.setBasePath(basePath);
		resp.setModuleSrc(moduleSrc);
		resp.setModuleName(m.moduleName);
		resp.setModuleVersion(m.moduleVersion);
		
		List<AA0315Item> oas2ItemList = getAA0315ItemList(rootNode, protocol, host, basePath);
		resp.setOpenApiList(oas2ItemList);
	
		return resp;
	}
	
	private static class Module{
		public String moduleName;
		public String moduleVersion;
	}
	
	private Module chechNeededData(JsonNode rootNode) {
		Module m = new Module();
		JsonNode oas2InfoNode = rootNode.get("info");//root.info
		if(oas2InfoNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info");
		}
		JsonNode oas2TitleNode = oas2InfoNode.get("title");//root.info.title
		if(oas2TitleNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info.title");
		}
		m.moduleName = oas2TitleNode.asText();
		
		//moduleVersion: 模組版本,root.info.version，若未出現此欄位，則 throw 1350 ([info.version] 為必填欄位)。
		JsonNode versionNode = oas2InfoNode.get("version");//root.info.version
		if(versionNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("info.version");
		}
		m.moduleVersion = versionNode.asText();
		return m;
	}

	private String createBasepath(JsonNode oas2BasePathNode) {
		String basePath = "";
		if (oas2BasePathNode != null) {
			basePath = oas2BasePathNode.asText();
			boolean flag = basePath.startsWith("/");
			if(!flag) {
				throw TsmpDpAaRtnCode._1352.throwing("basePath");
			}
		}
		return basePath;
	}

	public List<AA0315Item> getAA0315ItemList(JsonNode rootNode, String protocol, String host, String basePath) {
		List<AA0315Item> openApiList = new ArrayList<>();
		
		//openApiList: API清單,root.paths，若未傳入此欄位，則 throw 1350 ([paths] 為必填欄位)。需封裝成 AA0315Item 後填入
		JsonNode oas2PathsNode = rootNode.get("paths");//root.paths
		if(oas2PathsNode == null) {
			throw TsmpDpAaRtnCode._1350.throwing("paths");
		}
		
		Iterator<String> oas2PathIter = oas2PathsNode.fieldNames();
		while (oas2PathIter.hasNext()) {//多個Path
			AA0315Item oas2Item = new AA0315Item();
			
			//path: 來源URL,root.paths.{path}，若值非以 "/" 開頭則 throw 1352 ([path] 格式不正確)
			String path = oas2PathIter.next();
			if(!StringUtils.hasText(path) || !path.startsWith("/")) {
				throw TsmpDpAaRtnCode._1352.throwing("path");
			}
			//rearPath: 來源URL的最尾路徑,root.paths.{path}，截取最後一個"/"後的內容，ex: path 為 "/A/B/C" 則此欄位填入 "C"
			String[] arr = path.split("/");
			String rearPath = arr[arr.length - 1];
			
			JsonNode pathNode = oas2PathsNode.get(path);//root.paths.{path}
			Iterator<String> methodIter = pathNode.fieldNames();
			String summary = "";
			String description = "";
			List<String> methodList = new ArrayList<>();
			List<String> consumesList = new ArrayList<>();
			List<String> producesList = new ArrayList<>();
			while (methodIter.hasNext()) {//多個method
				//methods: Http Method,root.paths.{path}.{method}，需整理出不重複值的List，若有重複或是無元素，則 throw 1352 ([methods] 格式不正確)
				String method = methodIter.next();
				addMethod(method, methodList);
				
				JsonNode methodNode = pathNode.get(method);//root.paths.{path}.{method}
				
				
				if(methodNode != null) {
					/*
					 * summary: API名稱,root.paths.{path}.{method}.summary，在每個 method 中取第一個有值的 summary，
					 * 若都沒有 summary，則填入與 rearPath 相同的值
					 */
					summary = makeSummary(summary, methodNode);
					
					/*
					 * apiDesc: API說明,root.paths.{path}.{method}.description，在每個 method 中取第一個有值的 description，
					 * 若都沒有 description，則填入空白字串
					 */
					description = makeDescription(description, methodNode);
					
					//consumes: Http Consumes,root.paths.{path}.{method}.consumes，把每個 method 中的 consumes 值(字串陣列)，整理成一個不重複值的List
					addConsumesList(consumesList, methodNode);
					
					//produces: Http Produces,root.paths.{path}.{method}.produces，把每個 method 中的 produces 值(字串陣列)，整理成一個不重複值的List
					addProducesList(producesList,methodNode);
				}
			} // end while
			
			if(!StringUtils.hasText(summary)) {//若都沒有 summary，則填入與 rearPath 相同的值
				summary = rearPath;
			}
			//srcUrl: 完整的來源URL,protocol + host + basePath + path，若 protocol 為空值，則預設以"https://"代替。ex:"https://host/basePath/path
			String srcUrl = getSrcUrl(basePath, protocol, host, path);
			
			//methods: Http Method,root.paths.{path}.{method}，需整理出不重複值的List，若有重複或是無元素，則 throw 1352 ([methods] 格式不正確)
			if(methodList == null || methodList.isEmpty()) {
				throw TsmpDpAaRtnCode._1352.throwing("methods");
			}
 
			//headers: Http Header,暫不解析
			List<String> headersList = new ArrayList<>();
			
			//params: Http Parameter,暫不解析
			List<String> paramsList = new ArrayList<>();
			
			oas2Item.setSummary(summary);
			oas2Item.setRearPath(rearPath);
			oas2Item.setPath(path);
			oas2Item.setSrcUrl(srcUrl);
			oas2Item.setApiDesc(ServiceUtil.nvl(description));
			oas2Item.setSrcUrl(srcUrl);
			oas2Item.setMethods(methodList);
			oas2Item.setHeaders(headersList);
			oas2Item.setParams(paramsList);
			oas2Item.setConsumes(consumesList);
			oas2Item.setProduces(producesList);
			
			openApiList.add(oas2Item);
		}
		return openApiList;
	}

	private String getSrcUrl(String basePath, String protocol, String host, String path) {
		String srcUrl = "";
		if("/".equals(basePath)) {
			srcUrl = protocol + host + path;
		}else {
			srcUrl = protocol + host + basePath + path;
		}
		return srcUrl;
	}

	private void addProducesList(List<String> producesList, JsonNode methodNode) {
		JsonNode producesNode = methodNode.get("produces");//root.paths.{path}.{method}.produces
		if(producesNode != null && producesNode.isArray()) {//是否為陣列
			for (JsonNode objNode : producesNode) {
				String produce = objNode.asText();
				if(StringUtils.hasText(produce) && !producesList.contains(produce)) {
					producesList.add(produce);
				}
			}
		}
	}

	private void addConsumesList(List<String> consumesList, JsonNode methodNode) {
		JsonNode consumesNode = methodNode.get("consumes");//root.paths.{path}.{method}.consumes
		if(consumesNode != null && consumesNode.isArray()) {//是否為陣列
			for (JsonNode objNode : consumesNode) {
				String consume = objNode.asText();
				if(StringUtils.hasText(consume) && !consumesList.contains(consume)) {
					consumesList.add(consume);
				}
			}
		}
	}

	private String makeDescription(String description, JsonNode methodNode) {
		JsonNode descriptionNode = methodNode.get("description");//root.paths.{path}.{method}.description
		if(descriptionNode != null) {
			String descriptionTemp = descriptionNode.asText();
			if(!StringUtils.hasText(description) && StringUtils.hasText(descriptionTemp)) {
				description = descriptionTemp;
			}
		}
		return description;
	}

	private String makeSummary(String summary, JsonNode methodNode) {
		JsonNode summaryNode = methodNode.get("summary");//root.paths.{path}.{method}.summary
		if(summaryNode != null) {
			String summaryTemp = summaryNode.asText();
			if(!StringUtils.hasText(summary) && StringUtils.hasText(summaryTemp)) {
				summary = summaryTemp;
			}
		}
		return summary;
	}


	private void addMethod(String method, List<String> methodList) {
		if(StringUtils.hasText(method)) {
			if(methodList.contains(method)) {//有重複
				throw TsmpDpAaRtnCode._1352.throwing("methods");
			}else {
				if (httpMethodType.contains(method.toUpperCase())) {
					methodList.add(method);
				}
			}
		}
	}
}
