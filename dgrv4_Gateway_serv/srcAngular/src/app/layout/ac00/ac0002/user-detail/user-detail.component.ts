import { Component, OnInit, Input } from '@angular/core';
import { AA0003Resp } from '../../../../models/api/UserService/aa0003.interface';

@Component({
    selector: 'app-user-detail',
    templateUrl: './user-detail.component.html',
    styleUrls: ['./user-detail.component.css']
})
export class UserDetailComponent implements OnInit {

    @Input() data!: AA0003Resp;
    @Input() center: boolean = true;

    constructor() {
    }

    ngOnInit() {
    }
}
