
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';

@Injectable({
    providedIn: 'root'
})
export class MemListService {

    public get basePath(): string {
        return environment.isv4 ? 'dgrv4/v3' : 'tsmpdpaa/v3';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }


    /**
     * 刷新排程工作memList
     */
    refreshMemList(){
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.refreshMemList),
            ReqBody: {}
        }
        const path = `${this.basePath}/refreshMemList`;
        return this.api.npPost(path, body);
    }

}
