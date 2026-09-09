// SPA fallback for the /frontend2 app. TanStack Router owns paths like /frontend2/projects,
// which do not exist as S3 keys, so anything that is not a file is served the app shell.
// "Not a file" is decided the same way the Angular app's `*.*` behaviour decides it: by a
// dot in the last path segment.
function handler(event) {
    const request = event.request;
    const lastSegment = request.uri.split('/').pop();

    if (!lastSegment.includes('.')) {
        request.uri = '/frontend2/index.html';
    }

    return request;
}
