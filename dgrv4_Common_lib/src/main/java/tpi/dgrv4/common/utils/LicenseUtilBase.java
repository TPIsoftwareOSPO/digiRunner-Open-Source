package tpi.dgrv4.common.utils;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/***
 *  需要Autowired 此class 的必須掛 @Scope("prototype") 因為是多例
 */
@Component
@Scope("prototype") // non-Singleton
public class LicenseUtilBase {
	public String getEdition(String key){
		return "Express";
	}
	public LocalDate getExpiryDate(String key){
		return LocalDate.MAX;
	}
	public Long getNearWarnDays(String key){
		return 0L;
	}
	public Long getOverBufferDays(String key){
		return 0L;
	}
	public String getAccount(String key){
		return "TPI";
	}
	public String getEnv(String key) {
		return "DROS";
	}
	public String getValue(String key, LicenseType type){
		return "N/A";
	}
	public String encode(LicenseEditionType licenseEditionType, LocalDate expiryDate, long nearWarnDays,
				long overBufferDays){
		return null;
	}
	public String encode(LicenseEditionType licenseEditionType, LocalDate expiryDate, long nearWarnDays,
				long overBufferDays, String account){
		return null;
	}
	public String encode(LicenseEditionType licenseEditionType, LocalDate expiryDate, long nearWarnDays,
				long overBufferDays, List<LicenseField> filedList){
		return null;
	}
	public void initLicenseUtil(String key, List<LicenseType> list) {
		
		
	}
}
