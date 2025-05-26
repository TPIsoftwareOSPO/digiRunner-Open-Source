import { Observable } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ModuleService } from 'src/app/shared/services/api-module.service';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FileUpload } from 'primeng/primeng';
import { concatMap } from 'rxjs/operators';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
    selector: 'app-ac0421',
    templateUrl: './ac0421.component.html',
    styleUrls: ['./ac0421.component.css'],
    providers: [MessageService, ModuleService]
})
export class Ac0421Component extends BaseComponent {
    @ViewChild('upload') upload: FileUpload;
    uploadedFiles: any[] = [];
    url: string;
    netUrl: string;
    file: { name: string, size: number };
    fileBuffer: any;
    value: any;
    disabled: boolean = false;

    constructor(
        protected route: ActivatedRoute,
        protected tr: TransformMenuNamePipe,
        private messageService: MessageService,
        private moduleService: ModuleService,
        private toolService: ToolService
    ) {
        super(route, tr);
        this.url = this.moduleService.uploadNetModuleUrl;
        // this.url = 'http://192.168.1.156:48083/'; // for test
        // this.netUrl = 'tsmpnaa/04/naa0401'; // for test
    }

    openFileBrowser(event) {
        this.disabled = false;
        if (!this.file) this.file = { name: '', size: 0 };
        this.file.name = event.target.files[0].name;
        let isize = event.target.files[0].size / 1024
        this.file.size = (Math.round(isize * 100) / 100)
    }

    public uploadNetFiles(upload): void {
        if (this.toolService.isTokenExpired()) {
            this.toolService.refreshToken().pipe(
                concatMap(() => this.excuteUpload(upload))
            ).subscribe();
        } else {
            this.excuteUpload(upload).subscribe();
        }
    }

    excuteUpload(upload): Observable<boolean> {
        return Observable.create(obser => {
            let files = file && file.files && file.files.length ? file.files : $('#choose-file')[0].files;
            if (!files.length) return;

            let formData = new FormData();
            for (var i = 0, file; file = files[i]; ++i) {
                formData.append('file', file);
            }

            let xhr = new XMLHttpRequest();
            let token = this.toolService.getToken();
            let url = this.url;
            // let url = this.url + this.netUrl; // for test
            // console.log('url :', url)
            xhr.onreadystatechange = function () {
                if (xhr.readyState == XMLHttpRequest.DONE)
                    console.log('res naa0401 :', xhr.responseText)
            }

            xhr.open('POST', url, true);
            xhr.setRequestHeader("Authorization", `Bearer ${token}`)
            xhr.setRequestHeader("backAuth", `Bearer ${token}`)
            let self = this;
            xhr.addEventListener("error", function (evt) {
                self.messageService.add({ severity: 'error', summary: 'upload Error', detail: 'Upload Fail' });
            });

            if (xhr.upload)
                xhr.upload.onprogress = this.updateProgress.bind(this);

            xhr.onload = function (e) {
                if (this.status == 200) {
                    obser.next(true);
                    if (upload) {
                        upload.clear();
                    } else {
                        $('#choose-file').val('');
                        self.file = undefined;
                    }
                    self.messageService.add({ severity: 'info', summary: 'File Uploaded', detail: this.responseText });
                }
                self.disabled = false;
            };
            xhr.send(formData);  // multipart/form-data
        });
    }

    updateProgress(evt) {
        if (evt.lengthComputable) {
            let percentage = Math.round((evt.loaded / evt.total) * 100);
            // console.log(percentage);
            this.value = `${percentage}%`;
        }
    }

    onToastClose(event) {
        this.value = '';
    }
}