package tpi.dgrv4.dpaa.component;

import org.springframework.http.HttpMethod;
import tpi.dgrv4.dpaa.vo.AA0315Req;
import tpi.dgrv4.dpaa.vo.AA0315Resp;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DpOpenApiDocServiceIfs {
	Set<String> httpMethodType = Stream.of(HttpMethod.values()).map(HttpMethod::name).collect(Collectors.toSet());
		
	public AA0315Resp parseData(AA0315Req req ,AA0315Resp resp, String fileData, String extFileName) throws Exception;
 
}
