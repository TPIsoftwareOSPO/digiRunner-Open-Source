import { Component, OnInit, Input } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';

@Component({
    selector: 'app-event-detail',
    templateUrl: './event-detail.component.html',
    styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit {

    @Input() data?: FormParams;
    @Input() center: boolean = true;

    constructor() { }

    ngOnInit() {
    }

}
