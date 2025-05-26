import { AA0201HostReq, AA0201HostReqAddNo } from './../../../../models/api/ClientService/aa0201.interface';
import { Component, OnInit, ViewChild, ViewContainerRef, ComponentFactoryResolver, forwardRef, Input, ElementRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { HostInputDetailComponent } from '../host-input-detail/host-input-detail.component';

@Component({
    selector: 'app-host-input',
    templateUrl: './host-input.component.html',
    styleUrls: ['./host-input.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => HostInputComponent),
        multi: true
    }]
})
export class HostInputComponent implements OnInit, ControlValueAccessor {

    @ViewChild('hostInput', { read: ViewContainerRef, static: true }) hostInputRef!: ViewContainerRef;
    @Input() label?: string;
    @Input() buttonName?: string;
    @ViewChild('content') content!: ElementRef;
    onTouched!: () => void;
    onChange!: (value: any) => void;

    hosts: Array<AA0201HostReqAddNo> = new Array<AA0201HostReqAddNo>();
    value?: AA0201HostReq[];
    disabled?: boolean;
    hostnums: number = 0;

    constructor(
        private factoryResolver: ComponentFactoryResolver
    ) { }

    ngOnInit() {
    }

    writeValue(hosts: AA0201HostReq[]): void {
        this.value = hosts;
        if (this.value)
            this.value.forEach(val => this.addHostInput(val));
        else if (hosts != null)
            this.addHostInput();
    }

    registerOnChange(fn: (value: any) => void): void {
        this.onChange = fn
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        this.disabled = isDisabled;
        $(this.content.nativeElement).find('input , button').prop('disabled', true).off('click');
    }

    addHostInput(newhost?: AA0201HostReq) {
        // this.hostInputRef.clear();
        // var componentFactory = this.factoryResolver.resolveComponentFactory(HostInputDetailComponent);
        let componentRef = this.hostInputRef.createComponent(HostInputDetailComponent);
        if (newhost){
          this.hosts.push({ hostName: newhost.hostName, hostIP: newhost.hostIP, no: this.hostnums });
        }
        else
            this.hosts.push({ hostName: '', hostIP: '', no: this.hostnums });
        // this.componentRef.instance.hostnums = this.hostnums;
        componentRef.instance._ref = componentRef;
        componentRef.instance.no = this.hostnums;
        componentRef.instance.data = newhost;
        this.hostnums++;
        componentRef.instance.change.subscribe((res: AA0201HostReqAddNo) => {
            // console.log(this.hosts);
            // console.log(res.no);
            let idx = this.hosts.findIndex(x => x.no === res.no);
            if (!idx && this.hosts.length == 0) {
                this.hosts.push({ hostName: '', hostIP: '', no: this.hostnums })
            } else {
                let idx = this.hosts.findIndex(host => host.no === res.no);
                this.hosts[idx].hostName = res.hostName;
                this.hosts[idx].hostIP = res.hostIP;
                this.hosts[idx].no = res.no;
            }
            let newHost: AA0201HostReq[] = this.hosts.map(x => {
                return { hostName: x.hostName, hostIP: x.hostIP }
            });
            this.onChange(newHost);
        });
        componentRef.instance.remove.subscribe(no => {
            let idx = this.hosts.findIndex(host => host.no === no);
            this.hosts.splice(idx, 1);
            this.onChange(this.hosts);
        });
    }
}
