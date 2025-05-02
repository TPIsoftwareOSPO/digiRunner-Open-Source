export class Menu {
    main: string = '';
    icon?: string;
    enabled?: boolean;
    subs?: SubMenu[]
}
export class SubMenu {
    name: string = '';
    path?: string;
    enabled?: boolean;
    value ? :string;
    funcURL ?:string;
}
