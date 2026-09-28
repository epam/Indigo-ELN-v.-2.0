// SPA fallback, on the distribution's default behaviour. TanStack Router owns paths like
// /projects/<uuid>, which do not exist as S3 keys, so anything that is not a file is served
// the app shell. A viewer-request rewrite, not a redirect, so the browser keeps the URL the
// router needs to read.
//
// "Not a file" is a dot in the last path segment. Hashed assets never reach this function —
// they match the earlier /assets/* behaviour — so in practice this only has to let through
// the handful of dotted objects at the bucket root, /index.html among them.
function handler(event) {
    const request = event.request;
    const lastSegment = request.uri.split('/').pop();

    if (!lastSegment.includes('.')) {
        request.uri = '/index.html';
    }

    return request;
}
