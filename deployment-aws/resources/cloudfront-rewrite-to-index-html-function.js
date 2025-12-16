function handler(event) {
    const request = event.request;
    const uri = request.uri;
    if (!uri.match(/.*\.\w+$/)) {
        request.uri = '/index.html';
    }
    return request;
}
