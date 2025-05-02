package tpi.dgrv4.common.component.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * Request物件僅需繼承此類別,<br>
 * 並覆寫{@code provideConstraints}方法即可。
 * @author Kim
 */
public abstract class ReqValidator {

	private static ITPILogger logger;

	private ReqConstraintsCache constraintsCache;

	public ReqValidator() {
		this.constraintsCache = new ReqConstraintsCache(logger);
	}

	/**
	 * 取得Request的限制式, 用於Controller的before()回傳給前端驗證欄位<br>
	 * 若Cache中沒有限制式, 才由子類提供
	 * @return
	 */
	public List<BeforeControllerRespItem> constraints(String locale) {
		List<BeforeControllerRespItem> constraints = this.constraintsCache.getConstraints(getClass(), locale);
		if (constraints == null) {
			constraints = provideConstraints(locale);
			checkDuplicateField(constraints);
			this.constraintsCache.setConstraints(getClass(), constraints, locale);
		}
		return constraints;
	}

	/**
	 * 在進入Controller主要邏輯前執行Request欄位值的檢核<br>
	 * ※不允許子類別覆寫此方法
	 * @throws TsmpDpAaException
	 */
	public final void validate() throws TsmpDpAaException {
		// 這裡呼叫 constraints 可以帶入 null 是因為檢核結果的錯誤訊息並不是拿 BeforeControllerRespValue 裡面的 msg
		List<BeforeControllerRespItem> constraints = constraints(null);
		if (CollectionUtils.isEmpty(constraints)) {
			return;
		}

		BCRIVFactoryIfs factory;
		BCRIValidatorIfs validator;
		for (BeforeControllerRespItem item : constraints) {
			factory = BCRIVFactories.getVFactory(item);
			validator = factory.getValidator(this);
			validator.validate();
		}
	}

	/**
	 * 提供欄位限制式給前端實作表單驗證
	 * @param locale
	 * @return
	 */
	protected abstract List<BeforeControllerRespItem> provideConstraints(String locale);

	private void checkDuplicateField(List<BeforeControllerRespItem> constraints) {
		if (!CollectionUtils.isEmpty(constraints)) {
			Set<String> fields = new HashSet<>();
			String field = null;
			for (BeforeControllerRespItem c : constraints) {
				field = c.getField();
				if (fields.contains(field)) {
					this.logger.debug("重複的Field名稱: " + field);
					throw TsmpDpAaRtnCode._1297.throwing();
				}
				fields.add(field);
			}
		}
	}
	
	public static void setLogger(ITPILogger logger) {
		ReqValidator.logger = logger;
	}

}