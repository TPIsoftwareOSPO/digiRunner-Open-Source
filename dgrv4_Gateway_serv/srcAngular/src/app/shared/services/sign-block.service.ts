import { ToolService } from 'src/app/shared/services/tool.service';
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { ResGetSignBlock } from 'src/app/models/api/SignBlockService/signBlock.interface';
import { ResResetSignBlock } from 'src/app/models/api/SignBlockService/resetSignBlock.interface';
import { environment } from 'src/environments/environment';
// import { ResGetSignBlock } from 'src/app/models/api/SignBlockService/signBlock.interface';
// import { ResResetSignBlock } from 'src/app/models/api/SignBlockService/resetSignBlock.interface';

@Injectable({
    providedIn: 'root'
})
export class SignBlockService {

    moduleName:string = environment.isv4 ? 'dgrv4/' : 'tsmpdpaa/';

    public get basePath(): string {
        return this.moduleName;
    }

    constructor(
        private api: ApiBaseService
    ) {
    }

    public getTestSignBlock(token: string): Observable<ResGetSignBlock> {
        const path = `${this.basePath}getSignBlock`;
        return this.api.excuteSignBlockGet<ResGetSignBlock>(path, token);
    }

    //取得SignBlock
    public getSignBlock(): Observable<ResGetSignBlock> {
        const path = `${this.basePath}getSignBlock`;
        return this.api.excuteSignBlockGet<ResGetSignBlock>(path);
    }

    //重置SignBlock
    public resetSignBlock(): Observable<ResResetSignBlock> {
        const path = `${this.basePath}resetSignBlock`;
        return this.api.excuteSignBlockGet<ResResetSignBlock>(path);
    }

}
