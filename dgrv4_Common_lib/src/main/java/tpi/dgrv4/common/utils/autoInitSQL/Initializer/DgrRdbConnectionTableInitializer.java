package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.DgrRdbConnectionVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.DgrWebsiteVo;

@Service
public class DgrRdbConnectionTableInitializer {

	private List<DgrRdbConnectionVo> dgrRdbConnectionList = new LinkedList<>();

	public List<DgrRdbConnectionVo> insertDgrRdbConnection() {
		try {

			createDgrRdbConnectionVo("APIM-default-DB","APIM-default-DB","APIM-default-DB","APIM-default-DB");

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}

		return dgrRdbConnectionList;
	}


	protected void createDgrRdbConnectionVo(String connName, String jdbcUrl, String userName, String encMima) {
		DgrRdbConnectionVo vo = new DgrRdbConnectionVo();
		vo.setConnectionName(connName);
		vo.setJdbcUrl(jdbcUrl);
		vo.setUserName(userName);
		vo.setMima(encMima);
		
		dgrRdbConnectionList.add(vo);

	}

}
