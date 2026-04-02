const STATIC_RESOURCES = /.*[A-Z0-9]{8}\.\w+$/;
const STATIC_CORS_RESOURCES = /.*\.(js|woff|eot|ttf|svg)$/;
const FONT_RESOURCES = /\/media\/.*(A-Z0-9){8}\..*$/;
const KETCHER_STATIC_RESOURCES = /.*\/ketcher\/static\/.*$/;
const KETCHER_INDEX = /\/ketcher\/index\.html$/;
const INDEX = /\/index\.html$/;
const OTHER_RESOURCES = /.*\.\w+$/;

function handler(event) {
    const request = event.request, response = event.response;
    const uri = request.uri;
    let cacheControl;
    if (uri.match(STATIC_RESOURCES) || uri.match(FONT_RESOURCES) || uri.match(KETCHER_STATIC_RESOURCES)) {
        cacheControl = 'immutable, max-age=31536000';
        if (uri.match(STATIC_CORS_RESOURCES)) {
            response.headers['access-control-allow-origin'] = {value: '*'}
        }
    } else if (uri.match(KETCHER_INDEX)) {
        cacheControl = 'public, max-age=3600';
    } else if (!uri.match(INDEX) && uri.match(OTHER_RESOURCES)) {
        cacheControl = 'public, max-age=86400';
    } else {
        cacheControl = 'public, max-age=600';
        let csp = "default-src 'self'; object-src 'none'; frame-ancestors 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; connect-src 'self' https://cognito-idp.us-east-1.amazonaws.com; font-src 'self' https://fonts.gstatic.com; worker-src 'self' blob:; media-src 'self' data:; img-src 'self' blob:;";
        response.headers['content-security-policy'] = {value: csp};
    }
    const contentType = response.headers['content-type'];
    if (contentType != null && contentType.value != null && contentType.value.startsWith('text/html')) {
        response.headers['x-frame-options'] = {value: 'SAMEORIGIN'};
    }
    response.headers['cache-control'] = {value: cacheControl};
    response.headers['strict-transport-security'] = {value: 'max-age=63072000; includeSubDomains; preload'};
    response.headers['x-content-type-options'] = {value: 'nosniff'};

    return response;
}
