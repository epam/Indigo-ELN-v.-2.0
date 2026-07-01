function handler(event) {
    var response = event.response;
    var headers = response.headers;
    var uri = event.request.uri;

    headers['strict-transport-security'] = {value: 'max-age=63072000; includeSubDomains; preload'};
    headers['x-content-type-options'] = {value: 'nosniff'};

    // delete headers['x-amz-server-side-encryption'];
    // delete headers['x-amz-server-side-encryption-aws-kms-key-id'];
    // delete headers['x-amz-version-id'];
    // delete headers['x-amz-delete-marker'];
    // delete headers['x-amz-id-2'];
    // delete headers['x-amz-request-id'];

    var STATIC_RESOURCES = /.*[A-Z0-9]{8}\.\w+$/;
    var STATIC_CORS = /.*\.(js|woff|eot|ttf|svg)$/;
    var FONT_RESOURCES = /\/media\/.*[A-Z0-9]{8}\..*$/;
    var KETCHER_STATIC = /.*\/ketcher\/static\/.*$/;

    var cacheControl;
    if (uri === '/assets/ketcher/index.html') {
        cacheControl = 'public, max-age=3600';
        headers['x-frame-options'] = {value: 'SAMEORIGIN'};
    } else if (STATIC_RESOURCES.test(uri) || FONT_RESOURCES.test(uri) || KETCHER_STATIC.test(uri)) {
        cacheControl = 'immutable, max-age=31536000';
        if (STATIC_CORS.test(uri)) {
            headers['access-control-allow-origin'] = {value: '*'};
        }
    } else {
        cacheControl = 'public, max-age=86400';
    }

    headers['cache-control'] = {value: cacheControl};

    return response;
}
