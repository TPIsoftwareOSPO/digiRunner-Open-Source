export const environment = {
    production: true,
    hmr: false,
    isv4: true,
    reportUrl: location.protocol + '//' + location.hostname + ':8080',
    apiUrl: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : ''),
    netApiUrl: location.protocol + '//' + location.hostname + ':8080',
    subPath: 'ac4/',
    iframeDomain: location.protocol + '//' + location.hostname,
    dpPath: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : ''),
    cusHostName: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : ''),
};
