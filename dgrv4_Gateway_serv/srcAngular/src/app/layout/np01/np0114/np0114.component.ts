import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { AboutService } from '../../../shared/services/api-about.service';
import { MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0031Req } from 'src/app/models/api/AboutService/dpb0031.interface';

@Component({
    selector: 'app-np0114',
    templateUrl: './np0114.component.html',
    styleUrls: ['./np0114.component.css'],
    providers: [AboutService, MessageService]
})
export class Np0114Component extends BaseComponent implements OnInit {

    form!: FormGroup;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private _fb: FormBuilder,
        private _about: AboutService,
        private _messageService: MessageService,
        private _tool: ToolService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.init();
    }

    private init(): void {
        this.form = this._fb.group({
            seqId: new FormControl(0),
            aboutSubject: new FormControl(''),
            aboutDesc: new FormControl('')
        });
        this._about.queryAbout_0_ignore1298().subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this.form.patchValue(res.RespBody);
            }
        });
    }

    public async send(): Promise<void> {
        const codes = ['dialog.edit', 'about', 'message.success'];
        const dict = await this._tool.getDict(codes);
        let ReqBody = {
            seqId: this.form.get('seqId')!.value,
            aboutSubject: this.form.get('aboutSubject')!.value,
            aboutDesc: this.form.get('aboutDesc')!.value
        } as DPB0031Req
        this._about.saveAbout(ReqBody).subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this._messageService.add({ severity: 'info', summary: `${dict['dialog.edit']} ${dict['about']}`, detail: `${dict['dialog.edit']} ${dict['message.success']}` });
            }
        });
    }
}
