function handler(event) {
    const response = event.response;
    const headers = response.headers;
    const uri = event.request.uri;

    headers['strict-transport-security'] = {value: 'max-age=63072000; includeSubDomains; preload'};
    headers['x-content-type-options'] = {value: 'nosniff'};

    const STATIC_RESOURCES = /.*[A-Z0-9]{8}\.\w+$/;
    const STATIC_CORS = /.*\.(js|woff|eot|ttf|svg)$/;
    const FONT_RESOURCES = /\/media\/.*[A-Z0-9]{8}\..*$/;
    const KETCHER_STATIC = /.*\/ketcher\/static\/.*$/;

    let cacheControl;
    if (uri === '/assets/ketcher/index.html') {
        cacheControl = 'public, max-age=3600';
        headers['x-frame-options'] = {value: 'SAMEORIGIN'};
        // Ketcher is a third-party bundle that performs runtime code generation
        // (Ajv `new Function` validators, acorn parser). It requires 'unsafe-eval',
        // which the main app CSP intentionally forbids. Scope a dedicated,
        // minimal CSP to the Ketcher iframe document so it is still protected
        // without weakening the rest of the application.
        const ketcherCsp =
            "default-src 'self'; " +
            "object-src 'none'; " +
            "frame-ancestors 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "connect-src 'self'; " +
            "worker-src 'self' blob:; " +
            "media-src 'self' data:; " +
            "img-src 'self' blob: data:;";
        headers['content-security-policy'] = {value: ketcherCsp};
    } else if (
        STATIC_RESOURCES.test(uri) ||
        FONT_RESOURCES.test(uri) ||
        KETCHER_STATIC.test(uri)
    ) {
        cacheControl = 'immutable, max-age=31536000';
        if (STATIC_CORS.test(uri)) {
            headers['access-control-allow-origin'] = {value: '*'};
        }
    } else {
        cacheControl = 'public, max-age=86400';
    }

    const status = response.statusCode;
    if (status === 200 || status === 301 || status === 304) {
        headers['cache-control'] = {value: cacheControl};
    }

    return response;
}
