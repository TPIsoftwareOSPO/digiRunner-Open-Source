import { FormParams } from 'src/app/models/api/form-params.interface';
import { Observable } from 'rxjs';
import { Component, ViewChild, ViewContainerRef, ComponentFactoryResolver, Input } from "@angular/core";
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.css']
})
export class DialogComponent {

  @Input('visible') visible = false;
  @Input('title') title: string = '';
  @Input('closable') closable = true;
  @Input() contentStyle : any;
  @Input() width = 300;
  private componentRef;

  @ViewChild('content', { read: ViewContainerRef, static: true }) contentRef!: ViewContainerRef;


  constructor(
    // private factoryResolver: ComponentFactoryResolver,
    private ngxService: NgxUiLoaderService
  ) { }

  open(component, data?: any) {
    this.contentRef.clear();
    // var componentFactory = resolveComponentFactor(component);
    this.componentRef = this.contentRef.createComponent(component);
    this.componentRef.instance.data = data;
    // this.componentRef.instance.close = this.onHide.bind(this, data.afterCloseCallback);
    this.componentRef.instance.close = this.onHide.bind(this)
    this.visible = true;
  }

  // onHide(afterCloseCallback) {
  //   this.componentRef.destroy();
  //   this.visible = false;
  //   if (afterCloseCallback) afterCloseCallback();
  // }
  onHide(observable?: Observable<any>) {
    let data = this.componentRef.instance.data;
    this.componentRef.destroy();
    this.visible = false;
    if (observable) {
      observable.subscribe(r => {
        if (data && data.afterCloseCallback) data.afterCloseCallback(r);
        this.ngxService.stop();
      })
    } else if (data && data.afterCloseCallback) {
      data.afterCloseCallback();
      this.ngxService.stop();
    }
  }

  setWidth() {
    return this.width + 'px';
  }

}
