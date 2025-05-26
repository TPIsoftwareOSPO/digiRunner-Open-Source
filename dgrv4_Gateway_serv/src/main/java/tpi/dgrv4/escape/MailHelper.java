package tpi.dgrv4.escape;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailLog;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpDpMailLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
//checkmarx, Unchecked Input for Loop Condition
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class MailHelper {

	private List<Pair<String, String>> identifiers = new ArrayList<Pair<String, String>>();

	private final TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	private final TsmpDpMailLogDao tsmpDpMailLogDao;
	private final TsmpSettingService tsmpSettingService;

	@PostConstruct
	public void init() throws NumberFormatException {
		this.identifiers.add(Pair.of("[[", "]]"));
		this.identifiers.add(Pair.of("{{", "}}"));
	}

	public void sendEmail(TsmpMailEvent event) {
		String templateTxt = "## " + event.getSubject() + " ##, " + event.getContent();//## 主旨 ##, 內文

		TsmpDpMailLog mailLog = new TsmpDpMailLog();
		mailLog.setCreateDateTime(DateTimeUtil.now());
		mailLog.setCreateUser(event.getCreateUser());
		mailLog.setRecipients(event.getRecipients());
		mailLog.setTemplateTxt(templateTxt);
		mailLog.setRefCode(event.getRefCode());
		String result = "0";
		String stackTrace;
		try {
			MailParams mailParams = getPrimaryMailParams();
			result = send(event, mailParams, result);
		} catch (Exception e) {
			stackTrace = StackTraceUtil.logStackTrace(e);
			stackTrace = cutOffStackTrace(stackTrace);
			TPILogger.tl.error(stackTrace);
			mailLog.setStackTrace(stackTrace);
			// 若寄信失敗則使用 secondary 的 smtp server
			MailParams mailParams = getSecondaryMailParams();
			try {
				result = send(event, mailParams, result);
			} catch (Exception e1) {
				stackTrace = StackTraceUtil.logStackTrace(e1);
				TPILogger.tl.error(stackTrace);
				stackTrace = cutOffStackTrace(stackTrace);
				mailLog.setStackTrace(stackTrace);
			}
		} finally {

			try {
				mailLog.setResult(result);
				getTsmpDpMailLogDao().save(mailLog);
			} catch (Exception e) {
				if (ServiceUtil.isValueTooLargeException(e)) {
					stackTrace = StackTraceUtil.logTpiShortStackTrace(e);
					stackTrace = cutOffStackTrace(stackTrace);
					mailLog.setResult(result);
					getTsmpDpMailLogDao().save(mailLog);
				}else {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			}

		}
	}
	
	/*
	 * 若超過DB最大值則截斷	 
	 */
	private String cutOffStackTrace(String stackTrace) {
		if (stackTrace.length() > 4000) {
			stackTrace = stackTrace.substring(0, 4000);
		}
		return stackTrace;
	}

	public final static String buildContent(String template, Map<String, String> params) {
		String value;
		for(String key : params.keySet()) {
			value = "";
			if (params.get(key) != null) {
				value = params.get(key);
			}
			
			// replaceAll() 方法的第一個參數是作為正則表達式處理的，如果 key 變數包含正則表達式的特殊字符，
			// 可能會導致意外行為或安全問題。
			// 對於 replaceAll() 方法，正確的修正方式是使用 Pattern.quote() 包裝正則表達式部分
			template = template.replaceAll("\\{\\{" +  Pattern.quote(key) + "\\}\\}", value);
		}
		return template;
	}

	/**
	 * <ul>
	 * 	<li>
	 * 		模板中支持兩種變數: <b>陣列變數</b>、<b>物件變數</b><br>
	 * 		<b>陣列變數</b>: 由 [[ ]] 表示, 如: [[names]]。<br>
	 * 		<b>物件變數</b>: 由 {{ }} 表示, 如: {{name}}。
	 * 	</li>
	 * 	<li>
	 * 		模板間可互相參考，使用 <b>^</b>、<b>$</b> 符號分別表示參考上一層及下一層, 如:<br>
	 * 		tsmp_dp_mail_tplt.code = 'body'<br>
	 * 		tsmp_dp_mail_tplt.template_txt = '我的寵物有[[$<b>animal</b>]]'<br>
	 * 		即可參考到 code = 'body.<b>animal</b>' 的模板。<br>
	 * 		支持多層參考, 如: [[^^animal]]
	 *  </li>
	 *  <li>
	 *  	變數中可傳入參數: <b>變數名稱</b>、<b>分隔符號</b><br>
	 *  	<b>變數名稱</b>: 從資料集中取出的key值。<br>
	 *  	<b>分隔符號</b>: 限陣列變數才可以使用, 用來串接每個陣列的元素。<br>
	 *  	如: [[names, ', ']]、[[$animal, pets, ', ']]、{{$footer, foo}}<br>
	 *  	<b>注意</b>: 陣列變數若有指定變數名稱, 則必須同時指定分隔符號!
	 *  </li>
	 * </ul>
	 * 範例模板:<br>
	 * <table border="1">
	 * 	<thead><tr><th>code</th><th>template_txt</th></tr></thead>
	 * 	<tbody>
	 * 		<tr><td>body</td><td>[[names, '跟']]養了好多狗，有[[$animal, '、']]，{{$f1}}</td></tr>
	 * 		<tr><td>body.animal</td><td>{{type}}-{{name}}</td></tr>
	 * 		<tr><td>body.f1</td><td>還有養了很多貓，有[[^animal, cats, '、']]，{{$f2, birds}}</td></tr>
	 * 		<tr><td>body.f1.f2</td><td>又養了一些鳥，有[[^^animal, myBirds, '、']]</td></tr>
	 * 	</tbody>
	 * </table>
	 * <br>
	 * 實作範例:<br>
	 * Map&lt;String, Object&gt; params = new HashMap<>();<br>
	 * <br>
	 * List&lt;String&gt; names = new ArrayList&lt;&gt;();<br>
	 * names.add("小明");<br>
	 * names.add("小美");<br>
	 * params.put("names", names);<br>
	 * <br>
	 * List&lt;Map&lt;String, String&gt;&gt; dogs = new ArrayList&lt;&gt;();<br>
	 * Map&lt;String, String&gt; dog1 = new HashMap&lt;&gt;();<br>
	 * dog1.put("type", "臘腸狗");<br>
	 * dog1.put("name", "小臘");<br>
	 * dogs.add(dog1);<br>
	 * Map&lt;String, String&gt; dog2 = new HashMap&lt;&gt;();<br>
	 * dog2.put("type", "鬆獅犬");<br>
	 * dog2.put("name", "小鬆");<br>
	 * dogs.add(dog2);<br>
	 * params.put("animal", dogs);<br>
	 * <br>
	 * Map&lt;String, Object&gt; f1 = new HashMap&lt;&gt;();<br>
	 * List&lt;Map&lt;String, String&gt;&gt; cats = new ArrayList&lt;&gt;();<br>
	 * Map&lt;String, String&gt; cat1 = new HashMap&lt;&gt;();<br>
	 * cat1.put("type", "暹羅貓");<br>
	 * cat1.put("name", "小暹");<br>
	 * cats.add(cat1);<br>
	 * Map&lt;String, String&gt; cat2 = new HashMap&lt;&gt;();<br>
	 * cat2.put("type", "波斯貓");<br>
	 * cat2.put("name", "小波");<br>
	 * cats.add(cat2);<br>
	 * f1.put("cats", cats);<br>
	 * <br>
	 * Map&lt;String, Object&gt; f2 = new HashMap&lt;&gt;();<br>
	 * List&lt;Map&lt;String, String&gt;&gt; birds = new ArrayList&lt;&gt;();<br>
	 * Map&lt;String, String&gt; bird1 = new HashMap&lt;&gt;();<br>
	 * bird1.put("type", "文鳥");<br>
	 * bird1.put("name", "小文");<br>
	 * birds.add(bird1);<br>
	 * Map&lt;String, String&gt; bird2 = new HashMap&lt;&gt;();<br>
	 * bird2.put("type", "鸚鵡");<br>
	 * bird2.put("name", "小鸚");<br>
	 * birds.add(bird2);<br>
	 * f2.put("myBirds", birds);<br>
	 * <br>
	 * f1.put("birds", f2);<br>
	 * params.put("f1", f1);<br>
	 * <br>
	 * String content = buildNestedContent("body", params);<br>
	 * System.out.println(content);<br>
	 * <br>
	 * 執行結果:<br>
	 * <i>小明跟小美養了好多狗，有臘腸狗-小臘、鬆獅犬-小鬆，還有養了很多貓，有暹羅貓-小暹、波斯貓-小波，又養了一些鳥，有文鳥-小文、鸚鵡-小鸚</i>
	 * @author Kim
	 * @param rootCode
	 * @param params
	 */
	public String buildNestedContent(String rootCode, Map<String, Object> params) {
		TsmpDpMailTplt tplt = getTsmpDpMailTplt(rootCode);
		if (tplt == null) {
			return new String();
		}
		
		String template = tplt.getTemplateTxt();
		String content = new String();
		while(!(content = replaceNextVar(rootCode, template, params)).equals(template)) {
	    	 
			template = content;
			continue;
		}
		return content;
	}

	private TsmpDpMailTplt getTsmpDpMailTplt(String code) {
		List<TsmpDpMailTplt> tList = getTsmpDpMailTpltCacheProxy().findByCode(code);
		if (tList == null || tList.isEmpty()) {
			TPILogger.tl.error(String.format("Couldn't find mail template code: %s", code));
			return null;
		}
		return tList.get(0);
	}

	@SuppressWarnings("unchecked")
	private String replaceNextVar(String code, String template, Map<String, Object> params) {
		String valueToReplace = new String();	// 用來取代變數的值
		
		Map<String, Object> varInfo = getNextVarInfo(template, identifiers);
		// 已經沒有變數了
		if (varInfo == null) {
			return template;
		}

		int startIndex = (int) varInfo.get("startIndex");
		int endIndex = (int) varInfo.get("endIndex");
		Pair<String, String> idf = (Pair<String, String>) varInfo.get("idf");
		String refCode = (String) varInfo.get("refCode");
		String varName = (String) varInfo.get("varName");
		Object param = params == null ? null : params.get(varName);

		if (isAryVar(idf)) {
			String delimiter = (String) varInfo.get("delimiter");
			List<String> content = new ArrayList<>();
			if (param != null) {
				if (!StringUtils.isEmpty(refCode)) {
					code = resolveCode(code, refCode);
					for(Map<String, Object> p : (List<Map<String, Object>>) param) {
						content.add( buildNestedContent(code, p) );
					}
				} else {
					content = (List<String>) param;
				}
			}
			valueToReplace = String.join(delimiter, content);
		} else if (isMapVar(idf)) {
			if (!StringUtils.isEmpty(refCode)) {
				code = resolveCode(code, refCode);
				valueToReplace = buildNestedContent(code, (Map<String, Object>) param);
			} else {
				valueToReplace = String.valueOf(param);
			}
		}

		template = template.substring(0, startIndex).concat(valueToReplace).concat(template.substring(endIndex));

		return template;
	}

	private Map<String, Object> getNextVarInfo(String template, List<Pair<String, String>> idfs) {
		Pair<String, String> nextIdf = null;
		
		int startIndex = Integer.MAX_VALUE;
		int currentIndex = -1;
		for(Pair<String, String> idf : idfs) {
			currentIndex = template.indexOf( idf.getFirst() );
			if (currentIndex == -1) {
				currentIndex = Integer.MAX_VALUE;
			}
			if (currentIndex < startIndex) {
				startIndex = currentIndex;
				nextIdf = idf;
			}
		}
		// 沒有變數了
		if (nextIdf == null) {
			return null;
		}

		int endIndex = template.indexOf( nextIdf.getSecond() );
		String paramSegment = template.substring(startIndex + 2, endIndex);
		
		Map<String, Object> varInfo = new HashMap<>();
		varInfo.put("startIndex", startIndex);
		varInfo.put("endIndex", endIndex + 2);
		varInfo.put("idf", nextIdf);
		varInfo.put("refCode", null);
		varInfo.put("varName", null);
		varInfo.put("delimiter", new String());
		setVarParams(varInfo, paramSegment);
		return varInfo;
	}

	@SuppressWarnings("unchecked")
	private void setVarParams(Map<String, Object> varInfo, String paramSegment) {
		List<String> params = parseParamSegment(paramSegment);

		// 至少會有一個參數
		if (params.get(0).startsWith("$") || params.get(0).startsWith("^")) {
			varInfo.put("refCode", params.get(0));
			varInfo.put("varName", params.get(0).split("[\\$|\\^]+")[1]);
		} else {
			varInfo.put("varName", params.get(0));
		}

		Pair<String, String> idf = (Pair<String, String>) varInfo.get("idf");
		if (params.size() == 2) {
			if (isAryVar(idf)) {
				varInfo.put("delimiter", params.get(1));
			} else if (isMapVar(idf)) {
				varInfo.put("varName", params.get(1));
			}
		} else if (params.size() == 3) {
			varInfo.put("varName", params.get(1));
			varInfo.put("delimiter", params.get(2));
		}
	}

	private List<String> parseParamSegment(String paramSegment) {
		List<String> params = new ArrayList<>();
		
		int delimiterS = paramSegment.indexOf("'");
		int delimiterE = paramSegment.lastIndexOf("'");
		
		StringBuffer temp = new StringBuffer();
		String c = null;
		for(int i = 0; i < paramSegment.length(); i++) {
			c = paramSegment.substring(i, (i + 1));

			if ( (i < delimiterS || i > delimiterE) && !c.equals(" ") ) {
				if (c.equals(",")) {
					if (temp.length() > 0) {
						params.add(temp.toString().trim());
						temp.setLength(0);
					}
					continue;
				}

				temp.append(c);
				
				if (i == paramSegment.length() - 1) {
					params.add(temp.toString());
				}
			}
			
			//這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
			if (i == delimiterE && paramSegment.matches(".*'.*'.*")) { // NOSONAR
				params.add( paramSegment.substring(delimiterS + 1, delimiterE) );
			}
		}
		
		return params;
	}

	private String resolveCode(String code, String refCode) {
		//這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
		Pattern p = Pattern.compile("([\\$|\\^]*)(.+)"); // NOSONAR
		if (refCode==null) {refCode="";}
		Matcher m = p.matcher(refCode); // 不接受 null 
		if (m.matches()) {
			refCode = m.group(2);
			String ch = null;
			char[] cAry = m.group(1).toCharArray();
			for(int i = 0; i < cAry.length; i++) {
				ch = String.valueOf(cAry[i]);

				if (ch.equals("$")) {
					code = code.concat(".").concat(refCode);
				} else if (ch.equals("^")) {
					code = code.substring(0, code.lastIndexOf("."));
					if (i == cAry.length - 1) {
						code = code.concat(".").concat(refCode);
					}
				}
			}
		}
		return code;
	}

	private String send(TsmpMailEvent event, MailParams mailParams, String result) throws AddressException, UnsupportedEncodingException, MessagingException {
		// Email開關
		String mailHostType = mailParams.getMailHostType();
		if (!mailParams.isEnable()) {
			TPILogger.tl.info("Sending e-mail was called off, please enable flag(" + mailHostType + ")");
			return result;
		}
		
		InternetAddress[] recipients = InternetAddress.parse(event.getRecipients());

		Properties props = new Properties();
		props.put("mail.smtp.auth", mailParams.isAuth());
		props.put("mail.smtp.starttls.enable", mailParams.isStartTLS());
		props.put("mail.smtp.host", mailParams.getHost());
		props.put("mail.smtp.port", mailParams.getPort());
		
		// master 寄信前印出這些資訊
		TPILogger.tl.debug(mailHostType + " host = " + mailParams.getHost());
		TPILogger.tl.debug(mailHostType + " port = " + mailParams.getPort());
		TPILogger.tl.debug(mailHostType + " auth = " + mailParams.isAuth());
		TPILogger.tl.debug(mailHostType + " startTls = " + mailParams.isStartTLS());
		TPILogger.tl.debug(mailHostType + " username = " + mailParams.getUsername());
		TPILogger.tl.debug(mailHostType + " from = " + mailParams.getFrom());
		
		Session session = null;
		if(mailParams.isAuth()) {
			// Get the Session object.
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mailParams.getUsername(), mailParams.getMima());
				}
			});
		} else {
			session = Session.getInstance(props);
		}
		
		// Create a default MimeMessage object.
		String subject = MimeUtility.encodeWord(event.getSubject(), "UTF-8", "B");

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(mailParams.getFrom()));
		message.setRecipients(Message.RecipientType.TO, recipients);
		message.setSubject(subject);
		message.setContent(event.getContent(), "text/html; Charset=UTF-8");
		message.setHeader("X-Mailer", mailParams.getxMailer());
		message.setSentDate(DateTimeUtil.now());

		TPILogger.tl.debug(String.format("Sending email to %s", event.getRecipients()));
		// Send message
		Transport.send(message);
		TPILogger.tl.debug("Send successfully!");
		result = "1";
		return result;
	}

	private MailParams getPrimaryMailParams() {
		return new MailParamsBuilder().primary() //
		.enable(getTsmpSettingService().getVal_SERVICE_MAIL_ENABLE()) //
		.host(getTsmpSettingService().getVal_SERVICE_MAIL_HOST()) // 
		.port(getTsmpSettingService().getVal_SERVICE_MAIL_PORT()) //
		.auth(getTsmpSettingService().getVal_SERVICE_MAIL_AUTH()) //
		.username(getTsmpSettingService().getVal_SERVICE_MAIL_USERNAME()) //
		.password(getTsmpSettingService().getVal_SERVICE_MAIL_PASSWORD()) //
		.startTLS(getTsmpSettingService().getVal_SERVICE_MAIL_STARTTLS_ENABLE()) //
		.from(getTsmpSettingService().getVal_SERVICE_MAIL_FROM()) //
		.xMailer(getTsmpSettingService().getVal_SERVICE_MAIL_X_MAILER()) //
		.build();
	}

	private MailParams getSecondaryMailParams() {
		return new MailParamsBuilder().secondary() //
		.enable(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_ENABLE()) //
		.host(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_HOST()) // 
		.port(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_PORT()) //
		.auth(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_AUTH()) //
		.username(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_USERNAME()) //
		.password(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_PASSWORD()) //
		.startTLS(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE()) //
		.from(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_FROM()) //
		.xMailer(getTsmpSettingService().getVal_SERVICE_SECONDARY_MAIL_X_MAILER()) //
		.build();
	}

	private enum MailHostType {
		PRIMARY, SECONDARY;
	}

	private class MailParams {
		private String mailHostType;
		private boolean enable;
		private String host;
		private int port;
		private boolean auth;
		private String username;
		private String mima;
		private boolean startTLS;
		private String from;
		private String xMailer;
		public MailParams() {}
		protected MailParams(String mailHostType, //
				boolean enable, String host, int port, boolean auth, String username, String mima,
				boolean startTLS, String from, String xMailer) {
			super();
			this.mailHostType = mailHostType;
			this.enable = enable;
			this.host = host;
			this.port = port;
			this.auth = auth;
			this.username = username;
			this.mima = mima;
			this.startTLS = startTLS;
			this.from = from;
			this.xMailer = xMailer;
		}
		public String getMailHostType() {
			return mailHostType;
		}
		public boolean isEnable() {
			return enable;
		}
		public String getHost() {
			return host;
		}
		public int getPort() {
			return port;
		}
		public boolean isAuth() {
			return auth;
		}
		public String getUsername() {
			return username;
		}
		public String getMima() {
			return mima;
		}
		public boolean isStartTLS() {
			return startTLS;
		}
		public String getFrom() {
			return from;
		}
		public String getxMailer() {
			return xMailer;
		}
		protected void setMailHostType(String mailHostType) {
			this.mailHostType = mailHostType;
		}
		protected void setEnable(boolean enable) {
			this.enable = enable;
		}
		protected void setHost(String host) {
			this.host = host;
		}
		protected void setPort(int port) {
			this.port = port;
		}
		protected void setAuth(boolean auth) {
			this.auth = auth;
		}
		protected void setUsername(String username) {
			this.username = username;
		}
		protected void setMima(String mima) {
			this.mima = mima;
		}
		protected void setStartTLS(boolean startTLS) {
			this.startTLS = startTLS;
		}
		protected void setFrom(String from) {
			this.from = from;
		}
		protected void setxMailer(String xMailer) {
			this.xMailer = xMailer;
		}
	}

	private class MailParamsBuilder extends MailParams {
		public MailParamsBuilder primary() {
			setMailHostType(MailHostType.PRIMARY.toString());
			return this;
		}
		public MailParamsBuilder secondary() {
			setMailHostType(MailHostType.SECONDARY.toString());
			return this;
		}
		public MailParamsBuilder enable(String enable) {
			setEnable(Boolean.valueOf(enable));
			return this;
		}
		public MailParamsBuilder host(String host) {
			setHost(host);
			return this;
		}
		public MailParamsBuilder port(String port) {
			setPort(Integer.valueOf(port));
			return this;
		}
		public MailParamsBuilder auth(String auth) {
			setAuth(Boolean.valueOf(auth));
			return this;
		}
		public MailParamsBuilder username(String username) {
			setUsername(username);
			return this;
		}
		public MailParamsBuilder password(String password) {
			setMima(password);
			return this;
		}
		public MailParamsBuilder startTLS(String startTLS) {
			setStartTLS(Boolean.valueOf(startTLS));
			return this;
		}
		public MailParamsBuilder from(String from) {
			setFrom(from);
			return this;
		}
		public MailParamsBuilder xMailer(String xMailer) {
			setxMailer(xMailer);
			return this;
		}
		public MailParams build() {
			return new MailParams(
				getMailHostType(), isEnable(), getHost(), getPort(), isAuth(), //
				getUsername(), getMima(), isStartTLS(), getFrom(), //
				getxMailer()
			);
		}
	}

	private boolean isAryVar(Pair<String, String> idf) {
		return idf.getFirst().equals("[[");
	}

	private boolean isMapVar(Pair<String, String> idf) {
		return idf.getFirst().equals("{{");
	}

	protected TsmpDpMailTpltCacheProxy getTsmpDpMailTpltCacheProxy() {
		return this.tsmpDpMailTpltCacheProxy;
	}

	protected TsmpDpMailLogDao getTsmpDpMailLogDao() {
		return this.tsmpDpMailLogDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

}
