'use strict';

const crypto = require('crypto');

const HTML_TEMPLATE = {{INDEX_HTML}};

const CSP_TEMPLATE =
    "default-src 'self'; " +
    "object-src 'none'; " +
    "frame-ancestors 'self'; " +
    "script-src 'self' 'nonce-NONCE'; " +
    "style-src 'self' 'nonce-NONCE' https://fonts.googleapis.com; " +
    "connect-src 'self' https://cognito-idp.us-east-1.amazonaws.com; " +
    "font-src 'self' https://fonts.gstatic.com; " +
    "worker-src 'self' blob:; " +
    "media-src 'self' data:; " +
    "img-src 'self' blob: data:;";

exports.handler = async (event) => {
    const nonce = crypto.randomBytes(16).toString('base64');
    const body = HTML_TEMPLATE.replaceAll('CSP_NONCE_PLACEHOLDER', nonce);
    const csp = CSP_TEMPLATE.replaceAll('NONCE', nonce);

    return {
        status: '200',
        statusDescription: 'OK',
        headers: {
            'content-type': [{key: 'Content-Type', value: 'text/html; charset=utf-8'}],
            'cache-control': [{key: 'Cache-Control', value: 'no-store'}],
            'content-security-policy': [{key: 'Content-Security-Policy', value: csp}],
            'x-frame-options': [{key: 'X-Frame-Options', value: 'SAMEORIGIN'}],
            'strict-transport-security': [{key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains; preload'}],
            'x-content-type-options': [{key: 'X-Content-Type-Options', value: 'nosniff'}],
        },
        body: body,
        bodyEncoding: 'text',
    };
};
