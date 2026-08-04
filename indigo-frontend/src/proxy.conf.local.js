// Proxy for `npm run start-local`: the Keycloak build authenticates against the local Keycloak
// from deployment-compose, so API calls must go to that stack's nginx (http://localhost) too.
// The default src/proxy.conf.js targets the remote Cognito-protected dev server, which rejects
// local Keycloak tokens with 401.
module.exports = [
  {
    context: ['/api', '/internalapi'],
    target: 'http://localhost',
    secure: false,
    changeOrigin: true,
    logLevel: 'debug',
  },
];
