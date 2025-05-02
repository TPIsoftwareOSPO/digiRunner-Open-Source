import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { DPB0061Resp } from 'src/app/models/api/JobService/dpb0061.interface';
import { FileService } from 'src/app/shared/services/api-file.service';

@Component({
    selector: 'app-job-detail',
    templateUrl: './job-detail.component.html',
    styleUrls: ['./job-detail.component.css']
})
export class JobDetailComponent implements OnInit {

    @Input() data!: DPB0061Resp;
    @Input() center: boolean = true;
    @Output() changePage:EventEmitter<any> = new EventEmitter();

    stackTrace: string = '';

    constructor(
        private file: FileService
    ) { }

    ngOnInit() {
        this.stackTrace = this.data.stackTrace;
    }
    downloadFile(filePath: string, fileName: string) {
        let ReqBody = {
            filePath: filePath
        } as DPB0078Req;
        this.file.downloadFile(ReqBody).subscribe(res => {
            const reader = new FileReader();
            reader.onloadend = function () {
                // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
                //     window.navigator.msSaveBlob(res, fileName)
                // }
                // else {
                    const file = new File([res], fileName);
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
            reader.readAsText(res);
        });
    }
    changePageHandler(evt){
        this.changePage.emit(0);
    }
}
