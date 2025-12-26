export class CommonAPI {
    public static methods = [
        { label: 'GET', value: 'GET' },
        // { label: 'HEAD', value: 'HEAD' },
        { label: 'POST', value: 'POST' },
        { label: 'PUT', value: 'PUT' },
        { label: 'DELETE', value: 'DELETE' },
        // { label: 'CONNECT', value: 'CONNECT' },
        // { label: 'OPTIONS', value: 'OPTIONS' },
        // { label: 'TRACE', value: 'TRACE' },
        { label: 'PATCH', value: 'PATCH' }
    ];

    public static formats = [
        { label: 'SOAP', value: '0' },
        { label: 'JSON', value: '1' },
        { label: 'XML', value: '2' }
    ];
}