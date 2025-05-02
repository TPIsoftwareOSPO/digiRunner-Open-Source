import { DPB0128RespItem } from './../../../../models/api/ServerService/dpb0128.interface';
import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';



@Component({
    selector: 'app-paramitem',
    templateUrl: './param-item.component.html',
    styleUrls: ['./param-item.component.css'],
})

export class ParamItemComponent implements OnInit {

    @Input() paramItem?:DPB0128RespItem;

    constructor(
        private toolService: ToolService
    ) { }

    ngOnInit() { }

    downloadFile(data:any,fileName:string){
        let blob = new Blob([this.toolService.Base64Decoder(data)]);
        const reader = new FileReader();
        reader.onloadend = function () {
            // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
            //     window.navigator.msSaveBlob(data, fileName)
            // }
            // else {
                const file = new File([blob], fileName);
                const url = window.URL.createObjectURL(file);
                const a = document.createElement('a');
                document.body.appendChild(a);
                a.setAttribute('style', 'display: none');
                a.href = url;
                a.download = fileName;
                a.click();
                window.URL.revokeObjectURL(url);
                a.remove();
            // }
        }
        reader.readAsText(blob);
      }


}
