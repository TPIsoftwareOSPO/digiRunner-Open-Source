import { environment } from 'src/environments/environment';
import { AA0224Req } from './../../models/api/ClientService/aa0224.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { Observable, empty } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import * as model from '../../models/api/ClientService';
import { AA0201Req, ResAA0201Before } from 'src/app/models/api/ClientService/aa0201.interface';
import { TxID } from 'src/app/models/common.enum';
import { AA0202Req } from 'src/app/models/api/ClientService/aa0202.interface';
import { AA0203Req } from 'src/app/models/api/ClientService/aa0203.interface';
import { AA0204Req, ResAA0204Before } from 'src/app/models/api/ClientService/aa0204.interface';
import { AA0205Req } from 'src/app/models/api/ClientService/aa0205.interface';
import { AA0206Req } from 'src/app/models/api/ClientService/aa0206.interface';
import { AA0238Req } from 'src/app/models/api/ClientService/aa0238.interface';
import { Req_0235 } from 'src/app/models/api/ClientService/aa0235.interface';
import { AA0211Req } from 'src/app/models/api/ClientService/aa0211.interface';
import { Req_0213 } from 'src/app/models/api/ClientService/aa0213.interface';
import { AA0214Req } from 'src/app/models/api/ClientService/aa0214.interface';
import { AA0215Req } from 'src/app/models/api/ClientService/aa0215.interface';
import { AA0217Req, ResAA0217Before } from 'src/app/models/api/ClientService/aa0217.interface';
import { AA0216Req } from 'src/app/models/api/ClientService/aa0216.interface';
import { AA0219Req } from 'src/app/models/api/ClientService/aa0219.interface';
import { AA0218Req, ResAA0218Before } from 'src/app/models/api/ClientService/aa0218.interface';
import { AA0220Req, ResAA0220Before } from 'src/app/models/api/ClientService/aa0220.interface';
import { DPB0083Req, ResDPB0083, ReqDPB0083 } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { AA0232Req, ReqAA0232, ResAA0232 } from 'src/app/models/api/ClientService/aa0232.interface';
import { AA0228Req, ReqAA0228, ResAA0228 } from 'src/app/models/api/ClientService/aa0228.interface';
import { AA0227Req, ReqAA0227, ResAA0227 } from 'src/app/models/api/ClientService/aa0227.interface';
import { AA0229Req, ReqAA0229, ResAA0229 } from 'src/app/models/api/ClientService/aa0229.interface';
import { AA0226Req } from 'src/app/models/api/ClientService/aa0226.interface';
import { AA0230Req, ReqAA0230, ResAA0230 } from 'src/app/models/api/ClientService/aa0230.interface';
import { AA0231Req, ReqAA0231, ResAA0231, ResAA0231Before } from 'src/app/models/api/ClientService/aa0231.interface';
import { AA0234Req} from 'src/app/models/api/ClientService/aa0234.interface';
import { AA0233Req} from 'src/app/models/api/ClientService/aa0233.interface';

@Injectable()
export class ClientService {

    public get npBasePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService,
        private toolService: ToolService
    ) {

    }

    /**
     * before
     * AA0201: 新增Client
     * @param ReqBody
     */
    addClient_before(): Observable<ResAA0201Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addClient),
            ReqBody: {}
        } as model.ReqAA0201
        const path = `${this.npBasePath}/AA0201?before`;
        return this.api.npPost<ResAA0201Before>(path, body);
    }

    /**
     * AA0201: 新增Client
     * 在TSMP中新增Client (Channel)，Client可取得token再呼叫TSMP中部署的API。
     * 新增成功時返回clientID。
     * @param ReqBody AA0201Req
     */
    addClient(ReqBody: AA0201Req): Observable<model.ResAA0201> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addClient),
            ReqBody: ReqBody
        } as model.ReqAA0201
        const path = `${this.npBasePath}/AA0201`;
        return this.api.npPost<model.ResAA0201>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0202: 查詢ClientList
     * 在TSMP中查詢Client清單。
     * @param req_0202 Req_0202
     */
    queryClientList_ignore1298(ReqBody: AA0202Req): Observable<model.ResAA0202> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientList),
            ReqBody: ReqBody
        } as model.ReqAA0202
        const path = `${this.npBasePath}/AA0202`;
        return this.api.excuteNpPost_ignore1298<model.ResAA0202>(path, body);
    }

    /**
     * AA0202: 查詢ClientList
     * 在TSMP中查詢Client清單。
     * @param req_0202 Req_0202
     */
    queryClientList(ReqBody: AA0202Req): Observable<model.ResAA0202> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientList),
            ReqBody: ReqBody
        } as model.ReqAA0202
        const path = `${this.npBasePath}/AA0202`;
        return this.api.npPost<model.ResAA0202>(path, body);
    }

    /**
     * AA0203: 查詢ClientDetail
     * 在TSMP中查詢Client詳細資料。
     * @param ReqBody AA0203Req
     */
    queryClientDetail(ReqBody: AA0203Req): Observable<model.ResAA0203> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientDetail),
            ReqBody: ReqBody
        } as model.ReqAA0203
        const path = `${this.npBasePath}/AA0203`;
        return this.api.npPost<model.ResAA0203>(path, body);
    }

    /**
     * AA0204: 更新Client
     * 在TSMP中更新Client資料。
     * @param ReqBody AA0204Req
     */
    updateClient_before(): Observable<ResAA0204Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateClient),
            ReqBody: {}
        } as model.ReqAA0204
        const path = `${this.npBasePath}/AA0204?before`;
        return this.api.npPost<ResAA0204Before>(path, body);
    }

    /**
     * before
     * AA0204: 更新Client
     * 在TSMP中更新Client資料。
     * @param ReqBody AA0204Req
     */
     updateClient(ReqBody: AA0204Req): Observable<model.ResAA0204> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateClient),
            ReqBody: ReqBody
        } as model.ReqAA0204
        const path = `${this.npBasePath}/AA0204`;
        return this.api.npPost<model.ResAA0204>(path, body);
    }

    /**
     * AA0205: 刪除Client
     * 在TSMP中刪除Client資料及相關的Group, Host, Security資料。
     * @param ReqBody AA0205Req
     */
    deleteClientByClientId(ReqBody: AA0205Req): Observable<model.ResAA0205> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteClientByClientId),
            ReqBody: ReqBody
        } as model.ReqAA0205
        const path = `${this.npBasePath}/AA0205`;
        return this.api.npPost<model.ResAA0205>(path, body);
    }

    /**
     * AA0206: Client事件Log
     * TSMP Client可依需要將事件記錄在TSMP的資料庫中，這些事件必須事先預訂好。
     * @param req_0206 Req_0206
     */
    clientEventLog(ReqBody: AA0206Req): Observable<model.ResAA0206> {
        ReqBody.agent = ReqBody.agent.replace(/[;]/g, ',');
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.clientEventLog),
            ReqBody: ReqBody
        } as model.ReqAA0206
        if (!body.ReqBody.isLogin) return empty();
        const path = `${this.npBasePath}/AA0206`;
        return this.api.npPost<model.ResAA0206>(path, body);
    }

    /**
     * AA0211: 新增Group
     * 在TSMP中新增TSMP API Client Group，簡稱Group。系統可依Client的Group決定API使用權限。
     * 新增成功時返回groupID。
     * 權限設定時，只管Group是否有權使用該API，不管API的Module版本。換句話說，權限不會細到API要屬於哪個Module版本。
     * @param body ReqAA0211
     */
    addGroup(req_0211: AA0211Req): Observable<model.ResAA0211> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addGroup),
            ReqBody: req_0211
        } as model.ReqAA0211
        const path = `${this.npBasePath}/AA0211`;
        return this.api.npPost<model.ResAA0211>(path, body);
    }

    /**
     * AA0211: 新增Group before
     */

    addGroup_before(): Observable<model.ResAA0211> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addGroup),
            ReqBody: {}
        } as model.ReqAA0211
        const path = `${this.npBasePath}/AA0211?before`;
        return this.api.npPost<model.ResAA0211>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0238: 查詢GroupList
     * 在TSMP中查詢所有Group清單。
     * @param body AA0238Req
     */
    queryGroupList_0238_ignore1298(ReqBody: AA0238Req): Observable<model.ResAA0238> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryGroupList_0238),
            ReqBody: ReqBody
        } as model.ReqAA0238
        const path = `${this.npBasePath}/AA0238`;
        return this.api.excuteNpPost_ignore1298<model.ResAA0238>(path, body);
    }

    /**
     * AA0238: 查詢GroupList
     * 在TSMP中查詢所有Group清單。
     * @param body AA0238Req
     */
    queryGroupList_0238(ReqBody: AA0238Req): Observable<model.ResAA0238> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryGroupList_0238),
            ReqBody: ReqBody
        } as model.ReqAA0238
        const path = `${this.npBasePath}/AA0238`;
        return this.api.npPost<model.ResAA0238>(path, body);
    }

    /**
     * AA0235: 查詢群組所擁有的Module
     * @param body AA0235Req
     */
    queryModule_0235(ReqBody: Req_0235): Observable<model.ResAA0235> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModule_0235),
            ReqBody: ReqBody
        } as model.ReqAA0235
        const path = `${this.npBasePath}/AA0235`;
        return this.api.npPost<model.ResAA0235>(path, body);
    }

    /**
     * AA0213: 查詢GroupDetail
     * 在TSMP中查詢Group詳細資料。
     * @param body ReqAA0213
     */
    queryGroupDetail(req_0213: Req_0213): Observable<model.ResAA0213> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryGroupDetail),
            ReqBody: req_0213
        } as model.ReqAA0213
        const path = `${this.npBasePath}/AA0213`;
        return this.api.npPost<model.ResAA0213>(path, body);
    }

    /**
     * AA0214: 更新Group
     * 在TSMP中更新Group。
     * @param body ReqAA0214
     */
    updateGroup(req_0214: AA0214Req): Observable<model.ResAA0214> {
        // if (req_0214.newApiKeyList && req_0214.newApiKeyList.length == 0) req_0214.newApiKeyList = null;
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateGroup),
            ReqBody: this.formateReq0214(req_0214)
        } as model.ReqAA0214
        const path = `${this.npBasePath}/AA0214`;
        return this.api.npPost<model.ResAA0214>(path, body);
    }

    formateReq0214(req_0214: AA0214Req){
      for (var key in req_0214) {
        if (req_0214.hasOwnProperty(key)) {
          if (req_0214[key] === null || req_0214[key] == undefined || req_0214[key] === '')
          {
            delete req_0214[key];
          }
        }
      }
      return req_0214;
    }

    /**
     * AA0214: 更新Group before
     * 在TSMP中更新Group。
     * @param body ReqAA0214
     */
    updateGroup_before(): Observable<model.ResAA0214> {
        // if (req_0214.newApiKeyList && req_0214.newApiKeyList.length == 0) req_0214.newApiKeyList = null;
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateGroup),
            ReqBody: {}
        } as model.ReqAA0214
        const path = `${this.npBasePath}/AA0214?before`;
        return this.api.npPost<model.ResAA0214>(path, body);
    }

    /**
     * AA0215: 刪除Group
     * 在TSMP中刪除Group與相關API資料。若該Group已有Client參考，則不可刪除。
     * @param body ReqAA0215
     */
    deleteGroup(req_0215: AA0215Req): Observable<model.ResAA0215> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteGroup),
            ReqBody: req_0215
        } as model.ReqAA0215
        const path = `${this.npBasePath}/AA0215`;
        return this.api.npPost<model.ResAA0215>(path, body);
    }

    /**
     * AA0216: 更新Client端Group List
     * 在TSMP中經由Client端所選定之安全等級, 更新可選用之GroupList
     * @param body ReqAA0216
     */
    addClientGroupByClientId(ReqBody: AA0216Req): Observable<model.ResAA0216> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addClientGroupByClientId),
            ReqBody: ReqBody
        } as model.ReqAA0216
        const path = `${this.npBasePath}/AA0216`;
        return this.api.npPost<model.ResAA0216>(path, body);
    }

    /**
     * before
     * AA0217: 更新Client端安全等級
     * 在TSMP中經由Client端所選定之安全等級, 更新Client Security Level
     * @param body ReqAA0217
     */
    updateSecurityLVByClient_before(): Observable<ResAA0217Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateSecurityLVByClient),
            ReqBody: {}
        } as model.ReqAA0217
        const path = `${this.npBasePath}/AA0217?before`;
        return this.api.npPost<ResAA0217Before>(path, body);
    }

    /**
     * AA0217: 更新Client端安全等級
     * 在TSMP中經由Client端所選定之安全等級, 更新Client Security Level
     * @param body ReqAA0217
     */
    updateSecurityLVByClient(ReqBody: AA0217Req): Observable<model.ResAA0217> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateSecurityLVByClient),
            ReqBody: ReqBody
        } as model.ReqAA0217
        const path = `${this.npBasePath}/AA0217`;
        return this.api.npPost<model.ResAA0217>(path, body);
    }

    /**
     * before
     * AA0218: 更新Client端Token Setting
     * 1. 在TSMP中經由Client ID 更新Client端 Token 設定。
     * 2. 已鎖定之Client不可更新Token Setting, 需檢查TSMP_CLIENT.CLIENT_STATUS。
     * @param body ReqAA0218
     */
    updateTokenSettingByClient_before(): Observable<ResAA0218Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTokenSettingByClient),
            ReqBody: {}
        } as model.ReqAA0218
        const path = `${this.npBasePath}/AA0218?before`;
        return this.api.npPost<ResAA0218Before>(path, body);
    }

    /**
     * AA0218: 更新Client端Token Setting
     * 1. 在TSMP中經由Client ID 更新Client端 Token 設定。
     * 2. 已鎖定之Client不可更新Token Setting, 需檢查TSMP_CLIENT.CLIENT_STATUS。
     * @param body ReqAA0218
     */
    updateTokenSettingByClient(ReqBody: AA0218Req): Observable<model.ResAA0218> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTokenSettingByClient),
            ReqBody: ReqBody
        } as model.ReqAA0218
        const path = `${this.npBasePath}/AA0218`;
        return this.api.npPost<model.ResAA0218>(path, body);
    }

    /**
     * AA0219: 取得Client端Token Settings
     * 在TSMP中經由Client ID 取得Client端 Token 設定
     * @param body ReqAA0219
     */
    getTokenSettingByClient(ReqBody: AA0219Req): Observable<model.ResAA0219> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.getTokenSettingByClient),
            ReqBody: ReqBody
        } as model.ReqAA0219
        const path = `${this.npBasePath}/AA0219`;
        return this.api.npPost<model.ResAA0219>(path, body);
    }

    /**
     * before
     * AA0220: 更新Client端Status Setting
     * 在TSMP中經由Client ID 更新Client端 Status 以及 Password
     * @param body ReqAA0220
     */
    updateStatusSettingByClient_before(): Observable<ResAA0220Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateStatusSettingByClient),
            ReqBody: {}
        } as model.ReqAA0220
        const path = `${this.npBasePath}/AA0220?before`;
        return this.api.npPost<ResAA0220Before>(path, body);
    }

    /**
     * AA0220: 更新Client端Status Setting
     * 在TSMP中經由Client ID 更新Client端 Status 以及 Password
     * @param body ReqAA0220
     */
    updateStatusSettingByClient(ReqBody: AA0220Req): Observable<model.ResAA0220> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateStatusSettingByClient),
            ReqBody: ReqBody
        } as model.ReqAA0220
        const path = `${this.npBasePath}/AA0220`;
        return this.api.npPost<model.ResAA0220>(path, body);
    }

    /**
   * AA0221: 新增Virtul Group
   * @param body ReqAA0211
   */
    addVirtulGroup(req_0221: model.AA0221Req): Observable<model.ResAA0221> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addVirtulGroup),
            ReqBody: req_0221
        } as model.ReqAA0221
        const path = `${this.npBasePath}/AA0221`;
        return this.api.npPost<model.ResAA0221>(path, body);
    }

    /**
   * AA0221: 新增Virtul Group
   * @param body ReqAA0211
   */
    addVirtulGroup_before(): Observable<model.ResAA0221> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addVirtulGroup),
            ReqBody: {}
        } as model.ReqAA0221
        const path = `${this.npBasePath}/AA0221?before`;
        return this.api.npPost<model.ResAA0221>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0222: 查詢VirtulGroupList
     * 在TSMP中查詢所有Virtul Group清單。
     * @param body AA0222Req
     */
    queryVirtulGroupList_ignore1298(req_0222: model.AA0222Req): Observable<model.ResAA0222> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryVirtulGroupList),
            ReqBody: req_0222
        } as model.ReqAA0222
        const path = `${this.npBasePath}/AA0222`;
        return this.api.excuteNpPost_ignore1298<model.ResAA0222>(path, body);
    }

    /**
     * AA0222: 查詢VirtulGroupList
     * 在TSMP中查詢所有Virtul Group清單。
     * @param body AA0222Req
     */
    queryVirtulGroupList(req_0222: model.AA0222Req): Observable<model.ResAA0222> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryVirtulGroupList),
            ReqBody: req_0222
        } as model.ReqAA0222
        const path = `${this.npBasePath}/AA0222`;
        return this.api.npPost<model.ResAA0222>(path, body);
    }

    /**
     * AA0223: 查詢VirtulGroupDetail
     * 在TSMP中查詢Virtul Group詳細資料。
     * @param body Req_0223
     */
    queryVirtulGroupDetail(req_0223: model.AA0223Req): Observable<model.ResAA0223> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryVirtulGroupDetail),
            ReqBody: req_0223
        } as model.ReqAA0223
        const path = `${this.npBasePath}/AA0223`;
        return this.api.npPost<model.ResAA0223>(path, body);
    }


    /**
     * AA0237: 查詢VirtulGroupDetailApiList
     * 在TSMP中查詢Virtul Group ApiList
     * @param body Req_0237
     */
    queryVirtulGroupApiList(req_0237: model.AA0237Req): Observable<model.ResAA0237> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryVirtulGroupDetail),
            ReqBody: req_0237
        } as model.ReqAA0237
        const path = `${this.npBasePath}/AA0237`;
        return this.api.npPost<model.ResAA0237>(path, body);
    }

    /**
     * AA0224: 更新Virtul Group
     * 在TSMP中更新Virtul Group。
     * @param body Req_0224
     */
    updateVirtulGroup(req_0224: model.AA0224Req): Observable<model.ResAA0224> {
        // if (req_0224.newApiKeyList && req_0224.newApiKeyList.length == 0) req_0224.newApiKeyList = null;
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateVirtulGroup),
            ReqBody: this.formateReq0224(req_0224)
        } as model.ReqAA0224
        const path = `${this.npBasePath}/AA0224`;
        return this.api.npPost<model.ResAA0224>(path, body);
    }

    formateReq0224(req_0224: AA0224Req){
      for (var key in req_0224) {
        if (req_0224.hasOwnProperty(key)) {
          if (req_0224[key] === null || req_0224[key] == undefined || req_0224[key] === '')
          {
            delete req_0224[key];
          }
        }
      }
      return req_0224;
    }

    /**
     * AA0224: 更新Virtul Group before
     * 在TSMP中更新Virtul Group before。
     * @param body Req_0224
     */
    updateVirtulGroup_before(): Observable<model.ResAA0224> {
        // if (req_0224.newApiKeyList && req_0224.newApiKeyList.length == 0) req_0224.newApiKeyList = null;
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateVirtulGroup),
            ReqBody: {}
        } as model.ReqAA0224
        const path = `${this.npBasePath}/AA0224?before`;
        return this.api.npPost<model.ResAA0224>(path, body);
    }

    /**
     * AA0225: 刪除VirtulGroup
     * 在TSMP中刪除Virtul Group與相關API資料。若該Group已有Client參考，則不可刪除。
     * @param body ReqAA0225
     */
    deleteVirtulGroup(req_0225: model.AA0225Req): Observable<model.ResAA0225> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteVirtulGroup),
            ReqBody: req_0225
        } as model.ReqAA0225
        const path = `${this.npBasePath}/AA0225`;
        return this.api.npPost<model.ResAA0225>(path, body);
    }

    /**
     * AA0226: 更新Client端Virtul Group List
     * 在TSMP中經由Client端所選定之安全等級, 更新可選用之vGroupList
     * @param body ReqAA0226
     */
    addClientVGroupByClientId(ReqBody: AA0226Req): Observable<model.ResAA0226> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addClientVGroupByClientId),
            ReqBody: ReqBody
        } as model.ReqAA0226
        const path = `${this.npBasePath}/AA0226`;
        return this.api.npPost<model.ResAA0226>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0232: 查詢ClientEventLog
     * 查詢TSMP Client事件紀錄。
     * @param body ReqAA0232
     */
    queryClientEventLog_1_ignore1298(ReqBody: AA0232Req) {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientEventLog_1),
            ReqBody: ReqBody
        } as ReqAA0232
        const path = `${this.npBasePath}/AA0232`;
        return this.api.excuteNpPost_ignore1298<ResAA0232>(path, body);
    }

    /**
     * AA0232: 查詢ClientEventLog
     * 查詢TSMP Client事件紀錄。
     * @param body ReqAA0232
     */
    queryClientEventLog_1(ReqBody: AA0232Req) {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientEventLog_1),
            ReqBody: ReqBody
        } as ReqAA0232
        const path = `${this.npBasePath}/AA0232`;
        return this.api.npPost<ResAA0232>(path, body);
    }


    /**
     * AA0233: 查詢尚未勾選的Module
     * @param body AA0233Req
     */
    queryModule_0233(ReqBody: AA0233Req): Observable<model.ResAA0233> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientListByLike),
            ReqBody: ReqBody
        } as model.ReqAA0233
        const path = `${this.npBasePath}/AA0233`;
        return this.api.npPost<model.ResAA0233>(path, body);
    }


    /**
     * AA0234: 查詢尚未勾選的API
     * @param body AA0234Req
     */
    queryapi_0234(ReqBody: AA0234Req): Observable<model.ResAA0234> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientListByLike),
            ReqBody: ReqBody
        } as model.ReqAA0234
        const path = `${this.npBasePath}/AA0234`;
        return this.api.npPost<model.ResAA0234>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0083: 查找Client清單
     * 1. 查找憑證清單, 以CLIENT _ID, CLIENT_NAME, CLIENT_ALIAS作為模糊搜尋條件
     * 2. 列出CLIENT清單
     */
    queryClientListByLike_ignore1298(ReqBody: DPB0083Req): Observable<ResDPB0083> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientListByLike),
            ReqBody: ReqBody
        } as ReqDPB0083;
        const path = `${this.npBasePath}/DPB0083`;
        return this.api.excuteNpPost_ignore1298<ResDPB0083>(path, body);
    }

    /**
     * DPB0083: 查找Client清單
     * 1. 查找憑證清單, 以CLIENT _ID, CLIENT_NAME, CLIENT_ALIAS作為模糊搜尋條件
     * 2. 列出CLIENT清單
     */
    queryClientListByLike(ReqBody: DPB0083Req): Observable<ResDPB0083> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientListByLike),
            ReqBody: ReqBody
        } as ReqDPB0083;
        const path = `${this.npBasePath}/DPB0083`;
        return this.api.npPost<ResDPB0083>(path, body);
    }

    /**
     * AA0227: 授權設定-依ClientId刪除群組關係
     * @param ReqBody
     */
    deleteClientGroupByClientId(ReqBody: AA0227Req): Observable<ResAA0227> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteClientGroupByClientId),
            ReqBody: ReqBody
        } as ReqAA0227
        const path = `${this.npBasePath}/AA0227`;
        return this.api.npPost<ResAA0227>(path, body);
    }

    /**
     * AA0228: 依 cliendId 查詢該用戶未挑選之Group清單。(台新特殊邏輯)
     */
    queryGroupList_v3(ReqBody: AA0228Req): Observable<ResAA0228> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryGroupList_v3),
            ReqBody: ReqBody
        } as ReqAA0228
        const path = `${this.npBasePath}/AA0228`;
        return this.api.npPost<ResAA0228>(path, body);
    }

    /**
     * AA0229: 依 cliendId 查詢該用戶未挑選之VGroup清單。
     */
    queryVGroupList(ReqBody: AA0229Req): Observable<ResAA0229> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryVGroupList),
            ReqBody: ReqBody
        } as ReqAA0229
        const path = `${this.npBasePath}/AA0229`;
        return this.api.npPost<ResAA0229>(path, body);
    }

    /**
     * AA0230: 虛擬授權設定-依ClientId刪除群組關係
     */
    deleteClientVGroupByClientId(ReqBody: AA0230Req): Observable<ResAA0230> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteClientVGroupByClientId),
            ReqBody: ReqBody
        } as ReqAA0230
        const path = `${this.npBasePath}/AA0230`;
        return this.api.npPost<ResAA0227>(path, body);
    }

    /**
     * before
     * AA0231: 更新Client端Password Setting
     */
    updatePasswordSettingByClient_before(): Observable<ResAA0231Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updatePasswordSettingByClient),
            ReqBody: {}
        } as ReqAA0231
        const path = `${this.npBasePath}/AA0231?before`;
        return this.api.npPost<ResAA0231Before>(path, body);
    }

    /**
     * AA0231: 更新Client端Password Setting
     */
    updatePasswordSettingByClient(ReqBody: AA0231Req): Observable<ResAA0231> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updatePasswordSettingByClient),
            ReqBody: ReqBody
        } as ReqAA0231
        const path = `${this.npBasePath}/AA0231`;
        return this.api.npPost<ResAA0227>(path, body);
    }

}
