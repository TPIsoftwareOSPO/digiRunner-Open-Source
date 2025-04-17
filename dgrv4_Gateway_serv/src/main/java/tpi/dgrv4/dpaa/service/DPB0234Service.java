package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.*;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.*;

@Service
public class DPB0234Service {
    private TPILogger logger = TPILogger.tl;

    private DgrXApiKeyDao dgrXApiKeyDao;
    private DgrXApiKeyMapDao dgrXApiKeyMapDao;
    private TsmpClientDao tsmpClientDao;
    private TsmpClientGroupDao tsmpClientGroupDao;
    private TsmpGroupDao tsmpGroupDao;
    private TsmpGroupApiDao tsmpGroupApiDao;
    private TsmpApiDao tsmpApiDao;
    
    private static final String FLAG_KEYWORDS = "keyWords";
    private static final String FLAG_X_API_KEY = "xApiKey";

    @Autowired
    public DPB0234Service(DgrXApiKeyDao dgrXApiKeyDao, DgrXApiKeyMapDao dgrXApiKeyMapDao, TsmpClientDao tsmpClientDao,
			TsmpClientGroupDao tsmpClientGroupDao, TsmpGroupDao tsmpGroupDao, TsmpGroupApiDao tsmpGroupApiDao,
			TsmpApiDao tsmpApiDao) {
		super();
		this.dgrXApiKeyDao = dgrXApiKeyDao;
		this.dgrXApiKeyMapDao = dgrXApiKeyMapDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpApiDao = tsmpApiDao;
	}

	public static List<String> safelyConvertToStringList(Object obj) {
        if (!(obj instanceof List<?>)) {
            throw new IllegalArgumentException("傳入的物件不是 List 類型");
        }

        List<String> initialList = ((List<?>) obj).stream()
                .map(Object::toString)
                .distinct()
                .sorted() // 排序以確保較短的字串先被處理
                .toList();

        return removePrefixDuplicates(initialList);
    }

    private static List<String> removePrefixDuplicates(List<String> input) {
        List<String> result = new ArrayList<>();

        for (String current : input) {
            boolean shouldAdd = true;

            // 檢查當前字串是否是已加入結果中的任何字串的前綴
            for (String existing : result) {
                if (existing.startsWith(current) || current.startsWith(existing)) {
                    shouldAdd = false;
                    // 如果現有的字串比較長，替換成較短的
                    if (current.length() < existing.length()) {
                        result.remove(existing);
                        shouldAdd = true;
                    }
                    break;
                }
            }

            if (shouldAdd) {
                result.add(current);
            }
        }

        return result;
    }

    public DPB0234Resp getApiStatusByGroup(TsmpAuthorization authorization, DPB0234Req req) {
        String flag = req.getFlag();

        // 檢查flag值是否合法
        if (!FLAG_KEYWORDS.equals(flag) && !FLAG_X_API_KEY.equals(flag)) {
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        // 空值檢查
        if (FLAG_KEYWORDS.equals(flag)) {
            String keyWords = req.getKeyWords();
            if (keyWords == null || keyWords.trim().isEmpty()) {
                throw TsmpDpAaRtnCode._2025.throwing(FLAG_KEYWORDS);
            }
        } else if (FLAG_X_API_KEY.equals(flag)) {
            String xApiKey = req.getxApiKey();
            if (xApiKey == null || xApiKey.trim().isEmpty()) {
                throw TsmpDpAaRtnCode._2025.throwing(FLAG_X_API_KEY);
            }
        }

        String xApiKey = null;
        DPB0234Resp resp = null;

        switch (flag) {
            case FLAG_KEYWORDS:
                Object obj = this.parseString(req.getKeyWords());
                //判斷單值或多值
                if (obj instanceof String) {
                    // getByOneKeyWord
                    List<DPB0234RespItem> dataList = this.getApiStatusByOneKeyWord(String.valueOf(obj));
                    if (!dataList.isEmpty()) {
                        resp = new DPB0234Resp();
                        resp.setDataList(dataList);
                        int totalApis = this.calculateTotalApis(resp);
                        resp.setTotalApi(Integer.toString(totalApis));
                    } else {
                        throw TsmpDpAaRtnCode._1298.throwing();
                    }
                } else if (obj instanceof List) {
                    // 多關鍵字確保唯一性
                    List<String> keyWordsList = safelyConvertToStringList(obj);

                    // 保持順序並去重複
                    Set<DPB0234RespItem> uniqueResults = new LinkedHashSet<>();
                    for (String keyword : keyWordsList) {
                        List<DPB0234RespItem> items = this.getApiStatusByOneKeyWord(keyword);
                        uniqueResults.addAll(items);
                    }

                    if (!uniqueResults.isEmpty()) {
                        resp = new DPB0234Resp();
                        resp.setDataList(new ArrayList<>(uniqueResults));
                        int totalApis = this.calculateTotalApis(resp);
                        resp.setTotalApi(Integer.toString(totalApis));
                    } else {
                        throw TsmpDpAaRtnCode._1298.throwing();
                    }
                }
                break;
            case FLAG_X_API_KEY:
                //getBy xApiKey
                xApiKey = req.getxApiKey().trim();
                DPB0234ResponseFromXapiKey responseFromXapiKey = this.getGroupAndApiStatusByXApiKey(xApiKey);
                if (responseFromXapiKey != null) {
                    resp = new DPB0234Resp();
                    resp.setResponseFromXapiKey(responseFromXapiKey);
                } else {
                    throw TsmpDpAaRtnCode._1298.throwing();
                }
                break;
        }

        return resp;
    }

    public List<DPB0234RespItem> getApiStatusByOneKeyWord(String keyWord) {
        List<TsmpGroup> groupList = getGroupDao().
                findByGroupIdContainingOrGroupNameContainingOrGroupAliasContaining(keyWord, keyWord, keyWord);
        if (groupList.isEmpty())
            return Collections.emptyList();

        // 將所有GroupID進行查詢，傳回 1~多筆結果
        try {
            // 將多個 group 分別查詢
            List<DPB0234RespItem> dataList = new ArrayList<>();
            groupList.forEach(groupItem -> {
                DPB0234RespItem item = this.getOneApiStatusByGroupId(groupItem);
                dataList.add(item);
            });
            return dataList;
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    public DPB0234RespItem getOneApiStatusByGroupId(TsmpGroup group) {
        DPB0234RespItem respItem = null;
        try {
            String groupId = group.getGroupId();
            //使用 GROUP_ID 取 CLIENT_GROUP
            List<TsmpClientGroup> clientGroupList = getTsmpClientGroupDao().findByGroupId(groupId);

            //使用 CLIENT_GROUP 取 CLIENT
            List<TsmpClient> clientList = clientGroupList.stream()
                    .map(client -> getTsmpClientDao().findById(client.getClientId()))
                    .map(optional -> optional.orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            // 取得所有非空的 Client ID 列表
            List<String> clientIds = clientList.stream()
                    .map(TsmpClient::getClientId)
                    .filter(Objects::nonNull)
                    .filter(id -> !id.trim().isEmpty())
                    .toList();

            // 取得所有非空的 Client Name 列表
            List<String> clientNames = clientList.stream()
                    .map(TsmpClient::getClientName)
                    .filter(Objects::nonNull)
                    .filter(name -> !name.trim().isEmpty())
                    .toList();

            //使用 GROUP_ID 取其API
            List<TsmpApi> tsmpApiList = getTsmpApiDao().queryByTsmpGroupAPiGroupId(groupId);

            // apiDataList for DPB0234ClientDataItem
            List<DPB0234ApiDataItem> apiDataList = new ArrayList<>();
            tsmpApiList.forEach(tsmpApiElement -> {
                DPB0234ApiDataItem item = new DPB0234ApiDataItem();
                item.setApiName(tsmpApiElement.getApiName());
                item.setApiStatus(tsmpApiElement.getApiStatus());
                item.setApiPath(tsmpApiElement.getApiKey());
                item.setModuleName(tsmpApiElement.getModuleName());
                apiDataList.add(item);
            });

            // DPB0234RespItem
            respItem = new DPB0234RespItem();
            respItem.setGroupId(group.getGroupId());
            respItem.setGroupAlias(group.getGroupAlias());
            respItem.setGroupName(group.getGroupName());
            respItem.setClientIdList(clientIds);
            respItem.setClientNameList(clientNames);
            respItem.setApiDataList(apiDataList);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        return respItem;
    }

    public DPB0234ResponseFromXapiKey getGroupAndApiStatusByXApiKey(String xApiKey) {
        try {
            DPB0234ResponseFromXapiKey response = new DPB0234ResponseFromXapiKey();
            List<DPB0234GroupInfo> groupList = new ArrayList<>();
            int totalApiCount = 0;

            // 檢查xApiKey合法性
            String xApiKeyEn = TokenHelper.getXApiKeyEn(xApiKey);
            DgrXApiKey dgrXApiKey = this.getDgrXApiKeyDao().findFirstByApiKeyEn(xApiKeyEn);

            if (dgrXApiKey == null) {
                throw TsmpDpAaRtnCode._1298.throwing();
            }

            // 設置基本的XApiKey資訊
            response.setApiKeyId(dgrXApiKey.getApiKeyId().toString());
            response.setApiKeyMask(dgrXApiKey.getApiKeyMask());
            response.setApiKeyAlias(dgrXApiKey.getApiKeyAlias());
            response.setEffectiveAt(dgrXApiKey.getEffectiveAt().toString());
            response.setExpiredAt(dgrXApiKey.getExpiredAt().toString());
            response.setClientId(dgrXApiKey.getClientId());

            // 取得client資訊
            TsmpClient client = getTsmpClientDao().findById(dgrXApiKey.getClientId()).orElse(null);
            if (client != null) {
                response.setClientName(client.getClientName());
            }

            // 透過Map取xApiKey與Group的關聯
            Long apiKeyId = dgrXApiKey.getApiKeyId();
            List<DgrXApiKeyMap> xApiKeyMapList = this.getDgrXApiKeyMapDao()
                    .findByRefApiKeyId(apiKeyId);

            // 取得所有相關的Group
            List<TsmpGroup> groups = xApiKeyMapList.stream()
                    .map(xApiKeyMap -> getGroupDao().findById(xApiKeyMap.getGroupId()))
                    .flatMap(Optional::stream)
                    .distinct()
                    .toList();

            // 處理每個Group
            for (TsmpGroup group : groups) {
                DPB0234GroupInfo groupInfo = new DPB0234GroupInfo();
                groupInfo.setGroupId(group.getGroupId());
                groupInfo.setGroupName(group.getGroupName());
                groupInfo.setGroupAlias(group.getGroupAlias());

                // 取得該Group的API列表
                List<TsmpApi> tsmpApiList = getTsmpApiDao()
                        .queryByTsmpGroupAPiGroupId(group.getGroupId())
                        .stream()
                        .distinct()
                        .toList();

                // 建立API資料列表
                List<DPB0234ApiDataItem> apiDataList = tsmpApiList.stream()
                        .map(api -> {
                            DPB0234ApiDataItem item = new DPB0234ApiDataItem();
                            item.setApiName(api.getApiName());
                            item.setApiStatus(api.getApiStatus());
                            item.setApiPath(api.getApiKey());
                            item.setModuleName(api.getModuleName());
                            return item;
                        }).toList();

                groupInfo.setApiDataList(apiDataList);
                groupList.add(groupInfo);
                totalApiCount += apiDataList.size();
            }

            response.setGroupList(groupList);
            response.setTotalApi(String.valueOf(totalApiCount));

            return response;

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    public Object parseString(String input) {
        // 移除字串開頭和結尾的空白
        input = input.trim();

        // 檢查是否包含空格
        if (!input.contains(" ")) {
            // 如果沒有空格，直接返回原字串
            return input;
        } else {
            // 如果有空格，將字串分割並放入 List<String>
            List<String> data = Arrays.asList(input.split("\\s+"));
            return data;
        }
    }

    public int calculateTotalApis(DPB0234Resp response) {
        if (response == null) {
            return 0;
        }

        int totalApis = 0;

        // Calculate APIs from dataList (keyword search results)
        totalApis += Optional.ofNullable(response.getDataList())
                .stream()
                .flatMap(List::stream)
                .map(DPB0234RespItem::getApiDataList)
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .sum();

        // Calculate APIs from dataListFromXapiKey (X-API-KEY search results)
        totalApis += Optional.ofNullable(response.getDataListFromXapiKey())
                .stream()
                .flatMap(List::stream)
                .map(DPB0234RespItemFromXapiKey::getApiDataList)
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .sum();

        return totalApis;
    }

    protected DgrXApiKeyDao getDgrXApiKeyDao() {
        return dgrXApiKeyDao;
    }

    protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
        return dgrXApiKeyMapDao;
    }

    protected TsmpClientDao getTsmpClientDao() {
        return tsmpClientDao;
    }

    protected TsmpClientGroupDao getTsmpClientGroupDao() {
        return tsmpClientGroupDao;
    }

    protected TsmpGroupDao getGroupDao() {
        return tsmpGroupDao;
    }

    protected TsmpGroupApiDao getTsmpGroupApiDao() {
        return tsmpGroupApiDao;
    }

    protected TsmpApiDao getTsmpApiDao() {
        return tsmpApiDao;
    }
}
