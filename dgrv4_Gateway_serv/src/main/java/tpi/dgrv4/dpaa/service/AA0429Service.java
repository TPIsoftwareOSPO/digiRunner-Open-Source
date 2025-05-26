package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0423RespItem;
import tpi.dgrv4.dpaa.vo.AA0429Req;
import tpi.dgrv4.dpaa.vo.AA0429Resp;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0429Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public AA0429Service(TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	public AA0429Resp queryAllTargetSitListe(TsmpAuthorization authorization, AA0429Req req) {
		AA0429Resp resp = new AA0429Resp();
		try {

			List<String> srcUrlList = getTsmpApiRegDao().query_AA0429SrcUrl();
			List<String> ipSrcUrl1List = getTsmpApiRegDao().query_AA0429IpSrcUrl1();
			List<String> ipSrcUrl2List = getTsmpApiRegDao().query_AA0429IpSrcUrl2();
			List<String> ipSrcUrl3List = getTsmpApiRegDao().query_AA0429IpSrcUrl3();
			List<String> ipSrcUrl4List = getTsmpApiRegDao().query_AA0429IpSrcUrl4();
			List<String> ipSrcUrl5List = getTsmpApiRegDao().query_AA0429IpSrcUrl5();

			Set<String> set = new HashSet<>();
			set.addAll(b64DecodeAndGetTargetSite(srcUrlList));
			set.addAll(b64DecodeAndGetTargetSite(ipSrcUrl1List));
			set.addAll(b64DecodeAndGetTargetSite(ipSrcUrl2List));
			set.addAll(b64DecodeAndGetTargetSite(ipSrcUrl3List));
			set.addAll(b64DecodeAndGetTargetSite(ipSrcUrl4List));
			set.addAll(b64DecodeAndGetTargetSite(ipSrcUrl5List));

			set.removeIf(value -> !StringUtils.hasLength(value));

			List<String> urls = new ArrayList<>(set);
			if (urls.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			Collections.sort(urls);

			resp.setTargetSiteList(urls);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;

	}

	private List<String> b64DecodeAndGetTargetSite(List<String> list) {

		List<String> urls = new ArrayList<>();
		list.forEach(l -> {
			if (l.startsWith("b64.")) {
				String encodeString = l.split("b64.")[1];

				String[] base64String = encodeString.split("\\.");
				for (int i = 0; i < base64String.length; i++) {
					if (i % 2 == 0) {
						String plainText = new String(Base64Util.base64URLDecode(base64String[i + 1]));
						urls.add(getTargetSite(plainText));
					}
				}
			} else {
				urls.add(getTargetSite(l));
			}

		});

		return urls;
	}

	private String getTargetSite(String str) {
		String regex = "^(https?://[^/]+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		String result = "";
		if (matcher.find())
			result = matcher.group(1);

		return result;

	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}
}
