import { Component, OnInit, Input } from '@angular/core';
import { DPB0084certItem } from 'src/app/models/api/CertificateAuthorityService/dpb0084.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';

@Component({
    selector: 'app-ca-detail',
    templateUrl: './ca-detail.component.html',
    styleUrls: ['./ca-detail.component.css']
})
export class CADetailComponent implements OnInit {

    @Input() data!: DPB0084certItem;
    @Input() center: boolean = true;

    constructor() { }

    ngOnInit() {
    }

}
