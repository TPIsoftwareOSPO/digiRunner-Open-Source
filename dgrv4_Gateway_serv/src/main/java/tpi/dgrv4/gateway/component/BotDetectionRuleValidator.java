package tpi.dgrv4.gateway.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.entity.entity.DgrBotDetection;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao.BotDetectionType;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class BotDetectionRuleValidator implements ApplicationListener<ContextRefreshedEvent> {

    private DgrBotDetectionDao dgrBotDetectionDao;
    private TsmpSettingService tsmpSettingService;
    private BotDetectionCache botDetectionCache;

    private final AtomicReferenceArray<Boolean> isPrintingLog = new AtomicReferenceArray<>(
            new Boolean[]{false, false, false});

    private final AtomicReference<ConfigHolder> configHolder = new AtomicReference<>(
            new ConfigHolder(new ArrayList<>(), false));

    private final ExecutorService executorService;
    private final AtomicReference<ReloadConfig> pendingConfig;

    private static final List<String> IGNORE_APIS = Arrays.asList(
            "/dgrv4/", "/website/composer/", "/kibana/", "/http-api/", "/composer/swagger3.0/");

    @Autowired
	public BotDetectionRuleValidator(DgrBotDetectionDao dgrBotDetectionDao, TsmpSettingService tsmpSettingService,
			BotDetectionCache botDetectionCache) {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("bot-detection-reload-Thread");
            return thread;
        });
        this.pendingConfig = new AtomicReference<>(null);

		this.dgrBotDetectionDao = dgrBotDetectionDao;
		this.tsmpSettingService = tsmpSettingService;
		this.botDetectionCache = botDetectionCache;
    }

    public BotDetectionRuleValidateResult validate(HttpServletRequest request) {
        ConfigHolder currentConfig = configHolder.get();

        if (request == null) {
            return new BotDetectionRuleValidateResult(true, "Input request is null.", isPrintingLog(2));
        }

        String userAgent = Objects.toString(request.getHeader("User-Agent"), "");

        Optional<BotDetectionRuleValidateResult> opt = verificationEnable(request, userAgent, currentConfig);
        if (opt.isPresent()) {
            return opt.get();
        }

        // 同步檢查 Pattern
        for (Pattern pattern : currentConfig.patterns) {
            if (pattern.matcher(userAgent).matches()) {
                return new BotDetectionRuleValidateResult(true, 
                    generateSuccessMessage(pattern, request), 
                    isPrintingLog(1));
            }
        }

        return new BotDetectionRuleValidateResult(false, 
            generateErrorMessage(currentConfig.patterns, request),
            isPrintingLog(0));
    }

    private Optional<BotDetectionRuleValidateResult> verificationEnable(
            HttpServletRequest request, 
            String userAgent,
            ConfigHolder config) {

        String uri = request.getRequestURI();

        if (StringUtils.hasText(uri) && isIgnoreApi(uri)) {
            return Optional.of(new BotDetectionRuleValidateResult(true,
                    "Incoming request URI is a DGR API, skipping bot detection validation. The URI is: " + uri,
                    isPrintingLog(2)));
        }

        if (!config.isValidationEnabled) {
            return Optional.of(new BotDetectionRuleValidateResult(true, 
                "Bot Detection Rule Validation is Disable.",
                isPrintingLog(2)));
        }

        if (!StringUtils.hasText(userAgent)) {
            return Optional.of(new BotDetectionRuleValidateResult(true, 
                String.format("Skipping bot detection validation. User-Agent is empty. URI: %s", 
                    request.getRequestURI()),
                isPrintingLog(2)));
        }

        if (config.patterns.isEmpty()) {
            return Optional.of(new BotDetectionRuleValidateResult(true,
                    "Skipping bot detection validation. Bot Detection Rule Validation's Rule patterns is empty.",
                    isPrintingLog(2)));
        }

        return Optional.empty();
    }

    public static boolean isIgnoreApi(String uri) {
        return IGNORE_APIS.stream().anyMatch(uri::startsWith);
    }

    private String generateSuccessMessage(Pattern pattern, HttpServletRequest request) {
        if (pattern == null || request == null) {
            return "Error: Input parameters are null.";
        }

        return String.format("""
            The following request passed the rules:

            User-Agent:
            %s

            Passed Pattern:
            %s

            Request Details:

            Method: %s
            URI: %s
            Protocol: %s
            Remote Address: %s
            """,
            request.getHeader("User-Agent"),
            pattern.pattern(),
            request.getMethod(),
            request.getRequestURI(),
            request.getProtocol(),
            request.getRemoteAddr());
    }

    private String generateErrorMessage(List<Pattern> patterns, HttpServletRequest request) {
        if (patterns == null || request == null) {
            return "Error: Input parameters are null.";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Error: User-Agent is missing or empty.";
        }

        StringBuilder message = new StringBuilder(1024);
        message.append("The following request User-Agent did not pass the rules:\n\n")
               .append("Request User-Agent:\n")
               .append(userAgent)
               .append("\n\nAllow List patterns:\n");

        patterns.forEach(pattern -> 
            message.append("- ").append(pattern.pattern()).append("\n"));

        message.append("\nRequest Details:\n\n")
               .append("Method: ").append(request.getMethod()).append("\n")
               .append("URI: ").append(request.getRequestURI()).append("\n")
               .append("Protocol: ").append(request.getProtocol()).append("\n")
               .append("Remote Address: ").append(request.getRemoteAddr()).append("\n");

        return message.toString();
    }

    public CompletableFuture<Void> reloadAsync(List<Pattern> newPatterns, boolean newIsValidationEnabled) {
        return reloadAsyncInternal(BotDetectionUpdateType.UPDATE, Optional.of(newPatterns),
                Optional.of(newIsValidationEnabled));
    }

    public CompletableFuture<Void> reloadAsync(List<Pattern> newPatterns) {
        return reloadAsyncInternal(BotDetectionUpdateType.UPDATE, Optional.of(newPatterns), Optional.empty());
    }

    public CompletableFuture<Void> reloadAsync(boolean newIsValidationEnabled) {
        return reloadAsyncInternal(BotDetectionUpdateType.UPDATE, Optional.empty(),
                Optional.of(newIsValidationEnabled));
    }

    private CompletableFuture<Void> reloadAsyncInternal(BotDetectionUpdateType type,
            Optional<List<Pattern>> newPatterns, Optional<Boolean> newIsValidationEnabled) {

        pendingConfig.set(new ReloadConfig(newPatterns, newIsValidationEnabled));

        return CompletableFuture.runAsync(() -> {
            ReloadConfig config = pendingConfig.getAndSet(null);
            if (config != null) {
                updateConfig(config);
                logConfigurationUpdate(type);
            }
        }, executorService);
    }

    private void logConfigurationUpdate(BotDetectionUpdateType type) {
        ConfigHolder current = configHolder.get();
        switch (type) {
            case INITIALIZATION -> {
                if (Boolean.TRUE.equals(current.isValidationEnabled)) {
                    TPILogger.tl.debug("\n\nBot Detection Initialization Completed Successfully\n"
                            + current.toString());
                }
            }
            case UPDATE -> TPILogger.tl.debug("""
                    
                    Bot Detection Update Completed Successfully
                    %s%s""".formatted(current.toString(), getPrintingLogMsg(isPrintingLog)));
        }
    }

    private void updateConfig(ReloadConfig config) {
        List<Pattern> newPatterns = config.patterns.orElseGet(() -> 
            new ArrayList<>(configHolder.get().patterns));

        boolean newIsValidationEnabled = config.isValidationEnabled.orElseGet(() ->
            configHolder.get().isValidationEnabled);

        configHolder.set(new ConfigHolder(newPatterns, newIsValidationEnabled));
        
        // 當開關狀態改變時，清除所有快取
        if (newIsValidationEnabled != configHolder.get().isValidationEnabled) {
            botDetectionCache.invalidateAll();
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        updateFromDb(BotDetectionUpdateType.INITIALIZATION);
    }

    public void updateFromDb(BotDetectionUpdateType type) {
        // 初始化驗證開關
        boolean isValidationEnabled = "true".equalsIgnoreCase(
            tsmpSettingService.getVal_CHECK_BOT_DETECTION());

        // 設置日誌狀態
        setBotDetectionLogAsync(tsmpSettingService.getVal_BOT_DETECTION_LOG());

        // 獲取白名單規則
        List<DgrBotDetection> list = dgrBotDetectionDao
                .findByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(BotDetectionType.WHITELIST.getType());

        List<Pattern> patterns = compilePatterns(list);

        reloadAsyncInternal(type, Optional.of(patterns), Optional.of(isValidationEnabled));
    }

    private List<Pattern> compilePatterns(List<DgrBotDetection> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pattern> patterns = new ArrayList<>();
        List<String> invalidRules = new ArrayList<>();

        for (DgrBotDetection detection : list) {
            String rule = detection.getBotDetectionRule();
            try {
                patterns.add(Pattern.compile(rule));
            } catch (PatternSyntaxException e) {
                invalidRules.add(String.format("Rule: [%s] - Error: %s", rule, e.getDescription()));
            }
        }

        if (!invalidRules.isEmpty()) {
            TPILogger.tl.error("""
                
                Bot Detection Rule Validation Error
                
                The following rules contain invalid regular expression syntax:
                
                %s""".formatted(formatInvalidRules(invalidRules)));
        }

        return patterns;
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String formatInvalidRules(List<String> invalidRules) {
        return invalidRules.stream()
            .map(rule -> " • " + rule)
            .collect(Collectors.joining("\n")) + "\n";
    }

    public static class BotDetectionRuleValidateResult {
        private final boolean passed;
        private final String msg;
        private final boolean printingLog;

        public BotDetectionRuleValidateResult(boolean passed, String msg) {
            this(passed, msg, false);
        }

        public BotDetectionRuleValidateResult(boolean passed, String msg, boolean printingLog) {
            this.passed = passed;
            this.msg = msg;
            this.printingLog = printingLog;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getMsg() {
            return msg;
        }

        public boolean isPrintingLog() {
            return printingLog;
        }
    }

    private static class ConfigHolder {
        final List<Pattern> patterns;
        final boolean isValidationEnabled;

        ConfigHolder(List<Pattern> patterns, boolean isValidationEnabled) {
            this.patterns = List.copyOf(patterns);
            this.isValidationEnabled = isValidationEnabled;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder()
                .append("\nBot Detection Status: ")
                .append(isValidationEnabled ? "Enabled" : "Disabled")
                .append("\n\nBot Detection Allow List Rules:\n");

            if (patterns.isEmpty()) {
                sb.append("- No rules configured");
            } else {
                patterns.forEach(pattern -> 
                    sb.append("- ").append(pattern.pattern()).append("\n"));
            }

            return sb.toString();
        }
    }

    private String getPrintingLogMsg(AtomicReferenceArray<Boolean> isPrintingLog) {
        if (isPrintingLog.length() < 3) {
            return "Error: Invalid array length. Expected length: 3, Actual length: " + isPrintingLog.length();
        }

        return String.format("""
            
            BOT DETECTION LOG STATUS
            
            Print rejected request logs: %s
            Print accepted request logs: %s
            Print additional info logs: %s
            """,
            formatStatus(isPrintingLog.get(0)),
            formatStatus(isPrintingLog.get(1)),
            formatStatus(isPrintingLog.get(2)));
    }

    private String formatStatus(Boolean status) {
        return status == null ? "Unknown" : (status ? "Enabled" : "Disabled");
    }

    public boolean isValidationEnabled() {
        return configHolder.get().isValidationEnabled;
    }

    public List<Pattern> getPatterns() {
        return List.copyOf(configHolder.get().patterns);
    }

    private boolean isPrintingLog(int i) {
        return isPrintingLog.get(i);
    }

    public CompletableFuture<Void> setBotDetectionLogAsync(String input) {
        return CompletableFuture.runAsync(() -> {
            if (!StringUtils.hasText(input)) {
                for (int i = 0; i < 3; i++) {
                    isPrintingLog.set(i, false);
                }
                return;
            }

            List<Boolean> boolList = Arrays.stream(input.trim().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(part -> Boolean.parseBoolean(part.toLowerCase()))
                .collect(Collectors.toList());

            for (int i = 0; i < 3; i++) {
                isPrintingLog.set(i, i < boolList.size() ? boolList.get(i) : false);
            }
        }, executorService);
    }

    public enum BotDetectionUpdateType {
        INITIALIZATION, UPDATE
    }
}

class ReloadConfig {
    final Optional<List<Pattern>> patterns;
    final Optional<Boolean> isValidationEnabled;

    ReloadConfig(Optional<List<Pattern>> patterns, Optional<Boolean> isValidationEnabled) {
        this.patterns = patterns;
        this.isValidationEnabled = isValidationEnabled;
    }
}