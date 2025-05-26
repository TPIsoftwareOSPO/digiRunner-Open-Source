export enum EnumGrantType {
    password = 'password',
    client_credentials = 'client_credentials',
    refresh_token = 'refresh_token'
}
export interface ResToken {
    //為JWT Token，打API時當Bearer Token使用。
    access_token: string;
    //"bearer"
    token_type: string;
    //Token過期可用此取得新Token (僅"password"才有)
    refresh_token: string;
    //Token有效秒數
    expires_in: number;
    //Token有效範圍
    scope: string;
    node:string;
    //JWT Token ID
    jti: string;
    [key:string]:any;
}
export interface JWTHeader {
    alg: string;
    type: string;
}
export interface JWTPayload {
    aud: Array<string>;
    scope: Array<string>;
    exp: number;
    jti: string;
    client_id: string;
    user_name: string;
    authorities: Array<string>;
    server: string;
}
