package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.DgrWebsiteDetailVo;

@Service
public class DgrWebsiteDetailTableInitializer {

	private List<DgrWebsiteDetailVo> dgrWebsiteDetailVolist = new LinkedList<>();

	public List<DgrWebsiteDetailVo> insertDgrWebsiteDetail() {
		try {

			createDgrWebsiteDetailVo(1L, 1L, 100, "https://localhost:8081/website/dp/api/");
			createDgrWebsiteDetailVo(2L, 2L, 100, "http://localhost:8444/website/dp/");

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}

		return dgrWebsiteDetailVolist;
	}


	protected void createDgrWebsiteDetailVo(Long dgrWebsiteDetailId, Long dgrWebsiteId, Integer probability, String url) {
		DgrWebsiteDetailVo dgrWebsiteDetailVo = new DgrWebsiteDetailVo();
		dgrWebsiteDetailVo.setDgrWebsiteId(dgrWebsiteId);
		dgrWebsiteDetailVo.setDgrWebsiteDetailId(dgrWebsiteDetailId);
		dgrWebsiteDetailVo.setProbability(probability);
		dgrWebsiteDetailVo.setUrl(url);
		dgrWebsiteDetailVolist.add(dgrWebsiteDetailVo);
	}

}
