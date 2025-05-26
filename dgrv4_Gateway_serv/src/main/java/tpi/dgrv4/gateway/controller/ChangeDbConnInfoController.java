package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zaxxer.hikari.HikariDataSource;

import tpi.dgrv4.dpaa.service.ChangeDbConnInfoService;

@RestController

@Deprecated
public class ChangeDbConnInfoController {
	private ChangeDbConnInfoService service;
	private HikariDataSource dataSource;
	
	@Autowired
	public ChangeDbConnInfoController(ChangeDbConnInfoService service, HikariDataSource dataSource) {
		super();
		this.service = service;
		this.dataSource = dataSource;
	}

	// 此為測試用，目前棄用 
	@GetMapping(value = "/dgrv4/onlineConsole2/changeDbConnInfo", produces = "text/plain")
	public ResponseEntity<?> changeDbConnInfo(@RequestParam(value = "pw", required = false) String pw,
			@RequestParam(value = "un", required = false) String un,
			@RequestParam(value = "mode", required = false) String mode) { //判斷是不是要從API取資訊
		// 由於 API都會經過 gatwayfiler
		// ，必須避免被gatwayfiler的檢查影響，所以在gatwayfiler就先做掉了，但是為了方便追蹤，我就把Autowired留著了

		service.changeDbConnInfo(un,pw);
		StringBuffer sb = new StringBuffer();
		sb.append("========================================");
		sb.append("\n" + dataSource.getDriverClassName());
		sb.append("\njdbcUrl =  " + dataSource.getJdbcUrl());
		sb.append("\nusername = " + dataSource.getUsername());
		sb.append("\npassword = " + dataSource.getPassword());
		sb.append("\n========================================");
		return ResponseEntity.ok(sb);
	}

}
