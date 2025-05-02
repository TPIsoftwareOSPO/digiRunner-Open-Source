import { MultiSelectModule } from 'primeng/multiselect';
import { PanelModule } from 'primeng/panel';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { FieldsetModule } from 'primeng/fieldset';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ListboxModule } from 'primeng/listbox';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { CalendarModule } from 'primeng/calendar';
import { TooltipModule } from 'primeng/tooltip';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { TreeModule } from 'primeng/tree';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { CheckboxModule } from 'primeng/checkbox';
import { EditorModule } from 'primeng/editor';
import { TabViewModule } from 'primeng/tabview';
import { InputSwitchModule } from 'primeng/inputswitch';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import {DynamicDialogModule} from 'primeng/dynamicdialog';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {RadioButtonModule} from 'primeng/radiobutton';
import {CardModule} from 'primeng/card';
import { ChipModule } from 'primeng/chip';
import {SelectButtonModule} from 'primeng/selectbutton';
import {ChipsModule} from 'primeng/chips';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import {MegaMenuModule} from 'primeng/megamenu';
import {MenuModule} from 'primeng/menu';

const sharedModules: any[] = [
    InputTextModule,
    PanelModule,
    ButtonModule,
    FieldsetModule,
    TableModule,
    MultiSelectModule,
    DropdownModule,
    DialogModule,
    ListboxModule,
    InputTextareaModule,
    ToastModule,
    FileUploadModule,
    CalendarModule,
    TooltipModule,
    ToggleButtonModule,
    TreeModule,
    AutoCompleteModule,
    CheckboxModule,
    EditorModule,
    TabViewModule,
    InputSwitchModule,
    OverlayPanelModule,
    DynamicDialogModule,
    ConfirmDialogModule,
    RadioButtonModule,
    CardModule,
    ChipModule,
    SelectButtonModule,
    ChipsModule,
    ScrollPanelModule,
    MegaMenuModule,MenuModule
];
@NgModule({
    imports: [CommonModule],
    declarations: [],
    exports: sharedModules
})
export class PrimengModule {

}
