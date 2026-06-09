package tpi.dgrv4.dpaa.service.smartOnFhirProxyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.constant.SofProxySortBy;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0310Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDiversionDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxySearchReq;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SmartOnFhirProxySearchService {

    private TPILogger logger = TPILogger.tl;

    private DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao;
    private DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao;

    @Autowired
    public SmartOnFhirProxySearchService(
            DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao,
            DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao) {
        this.dgrSmartOnFhirProxyDao = dgrSmartOnFhirProxyDao;
        this.dgrSmartOnFhirProxyDiversionDao = dgrSmartOnFhirProxyDiversionDao;
    }

    public DPB0310Resp querySmartOnFhirProxyList(TsmpAuthorization auth, SmartOnFhirProxySearchReq req) {
        DPB0310Resp resp = new DPB0310Resp();

        try {
            // 驗證分頁參數
            validatePaginationParams(req);

            // 建立分頁與排序參數
            Pageable pageable = createPageable(req);

            // 查詢主表資料
            Page<DgrSmartOnFhirProxy> page = getDgrSmartOnFhirProxyDao().findByFiltersWithPagination(
                    req.getKeywords(),
                    req.getSofProxyStatus(),
                    pageable);

            // 批次查詢關聯的 Diversion 資料
            Map<Long, List<SmartOnFhirProxyDiversionDto>> diversionMap = queryDiversionMap(page.getContent());

            // 組裝 DTO
            List<SmartOnFhirProxyDto> dtoList = convertToDto(page.getContent(), diversionMap);

            // 設置回應
            resp.setContent(dtoList);
            resp.setTotalElements(page.getTotalElements());
            resp.setTotalPages(page.getTotalPages());
            resp.setNumber(page.getNumber());
            resp.setSize(page.getSize());

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        return resp;
    }

    private void validatePaginationParams(SmartOnFhirProxySearchReq req) {

        if (req.getPageNum() == null || req.getPageNum() < 0) {

            String fieldName = SmartOnFhirProxySearchReq.Fields.pageNum;
            String msg = "Field '" + fieldName + "' is invalid: " + req.getPageNum() + ". Page number must be >= 0.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        if (req.getPageSize() == null || req.getPageSize() <= 0) {

            String fieldName = SmartOnFhirProxySearchReq.Fields.pageSize;
            String msg = "Field '" + fieldName + "' is invalid: " + req.getPageSize() + ". Page size must be > 0.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // 限制 pageSize 最大值
        if (req.getPageSize() > 1000) {
            req.setPageSize(1000);
        }
    }

    private Pageable createPageable(SmartOnFhirProxySearchReq req) {

        String sortBy = req.getSortBy();
        SofProxySortBy sofProxySortBy = SofProxySortBy.SOF_PROXY_ID;

        if (StringUtils.hasText(sortBy)) {

            Optional<SofProxySortBy> optionalSofProxySortBy = SofProxySortBy.fromKey(sortBy);

            if (optionalSofProxySortBy.isPresent()) {
                sofProxySortBy = optionalSofProxySortBy.get();
            } else {
                String fieldName = SmartOnFhirProxySearchReq.Fields.sortBy;
                String msg = "Field '" + fieldName + "' is invalid: " + sortBy + ". Allowed values are: "
                        + SofProxySortBy.allowedKeys().toString();
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }
        }

        // 建立排序物件
        Sort.Direction direction = Sort.Direction.fromOptionalString(req.getSortOrder()).orElse(Sort.Direction.DESC);

        Sort sort = Sort.by(direction, sofProxySortBy.key());

        return PageRequest.of(req.getPageNum(), req.getPageSize(), sort);
    }

    private Map<Long, List<SmartOnFhirProxyDiversionDto>> queryDiversionMap(List<DgrSmartOnFhirProxy> proxyList) {
        if (proxyList == null || proxyList.isEmpty()) {
            return new HashMap<>();
        }

        // 收集所有 sofProxyId
        List<Long> proxyIds = proxyList.stream()
                .map(DgrSmartOnFhirProxy::getSofProxyId)
                .collect(Collectors.toList());

        // 批次查詢 Diversion
        List<DgrSmartOnFhirProxyDiversion> diversionList = getDgrSmartOnFhirProxyDiversionDao()
                .findBySofProxyIdIn(proxyIds);

        // 轉換為 DTO 並按 sofProxyId 分組
        Map<Long, List<SmartOnFhirProxyDiversionDto>> diversionMap = new HashMap<>();
        for (DgrSmartOnFhirProxyDiversion diversion : diversionList) {
            Long proxyId = diversion.getSofProxyId();
            if (!diversionMap.containsKey(proxyId)) {
                diversionMap.put(proxyId, new ArrayList<>());
            }
            diversionMap.get(proxyId).add(convertDiversionToDto(diversion));
        }

        return diversionMap;
    }

    private List<SmartOnFhirProxyDto> convertToDto(
            List<DgrSmartOnFhirProxy> entityList,
            Map<Long, List<SmartOnFhirProxyDiversionDto>> diversionMap) {

        List<SmartOnFhirProxyDto> dtoList = new ArrayList<>();

        for (DgrSmartOnFhirProxy entity : entityList) {
            SmartOnFhirProxyDto dto = new SmartOnFhirProxyDto();

            dto.setSofProxyId(IdCodec.toString(entity.getSofProxyId()));
            dto.setSofProxyName(entity.getSofProxyName());

            // 設置狀態
            if (entity.getSofProxyStatus() != null) {
                dto.setSofProxyStatus(entity.getSofProxyStatus().getCode());
                dto.setSofProxyStatusName(entity.getSofProxyStatus().getName());
            }

            dto.setSofProxyRemark(entity.getSofProxyRemark());

            // 設置其他安全性設定
            if (entity.getSofProxyAccessToken() != null) {
                dto.setSofProxyAccessToken(entity.getSofProxyAccessToken().getCode());
            }
            if (entity.getSofProxySqlInjection() != null) {
                dto.setSofProxySqlInjection(entity.getSofProxySqlInjection().getCode());
            }
            if (entity.getSofProxyTraffic() != null) {
                dto.setSofProxyTraffic(entity.getSofProxyTraffic().getCode());
            }
            if (entity.getSofProxyXss() != null) {
                dto.setSofProxyXss(entity.getSofProxyXss().getCode());
            }
            if (entity.getSofProxyXxe() != null) {
                dto.setSofProxyXxe(entity.getSofProxyXxe().getCode());
            }
            if (entity.getSofProxyShowLog() != null) {
                dto.setSofProxyShowLog(entity.getSofProxyShowLog().getCode());
            }
            if (entity.getSofProxySticky() != null) {
                dto.setSofProxySticky(entity.getSofProxySticky().getCode());
            }
            if (entity.getSofProxyUrlRewrite() != null) {
                dto.setSofProxyUrlRewrite(entity.getSofProxyUrlRewrite().getCode());
            }

            dto.setSofProxyTps(entity.getSofProxyTps());
            dto.setSofProxyIgnoreApi(entity.getSofProxyIgnoreApi());
            dto.setSofProxyClientId(entity.getSofProxyClientId());

            // 設置關聯的 Diversion 列表
            List<SmartOnFhirProxyDiversionDto> diversionList = diversionMap.get(entity.getSofProxyId());
            dto.setDiversionList(diversionList != null ? diversionList : new ArrayList<>());

            dto.setCreateDateTime(entity.getCreateDateTime());
            dto.setCreateUser(entity.getCreateUser());
            dto.setUpdateDateTime(entity.getUpdateDateTime());
            dto.setUpdateUser(entity.getUpdateUser());
            dto.setVersion((entity.getVersion().toString()));

            dtoList.add(dto);
        }

        return dtoList;
    }

    private SmartOnFhirProxyDiversionDto convertDiversionToDto(DgrSmartOnFhirProxyDiversion entity) {
        SmartOnFhirProxyDiversionDto dto = new SmartOnFhirProxyDiversionDto();

        dto.setSofProxyDiversionId(IdCodec.toString(entity.getSofProxyDiversionId()));
        dto.setSofProxyId(IdCodec.toString(entity.getSofProxyId()));
        dto.setSofProxyDiversionProbability(entity.getSofProxyDiversionProbability());
        dto.setSofProxyDiversionUrl(
                new SmartOnFhirProxyRouteContext.ParsedUrl(
                        entity.getSofProxyDiversionUrl(),
                        entity.getSofProxyDiversionFhirBasePath()).toFullUrl());
        dto.setCreateDateTime(entity.getCreateDateTime());
        dto.setCreateUser(entity.getCreateUser());
        dto.setUpdateDateTime(entity.getUpdateDateTime());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setVersion((entity.getVersion().toString()));

        return dto;
    }

    protected DgrSmartOnFhirProxyDao getDgrSmartOnFhirProxyDao() {
        return dgrSmartOnFhirProxyDao;
    }

    protected DgrSmartOnFhirProxyDiversionDao getDgrSmartOnFhirProxyDiversionDao() {
        return dgrSmartOnFhirProxyDiversionDao;
    }
}
