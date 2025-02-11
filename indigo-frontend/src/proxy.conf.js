module.exports = [
  {
    context: ['/api'],
    target: 'https://indigo-eln.test.lifescience.opensource.epam.com',
    secure: false,
    changeOrigin: true,
    logLevel: 'debug',
    configure: (proxy, _options) => {
      proxy.on('proxyReq', (proxyReq, req, _res) => {
        proxyReq.setHeader(
          'Origin',
          'https://indigo-eln.test.lifescience.opensource.epam.com',
        );
      });
      // TODO check if there is need
      proxy.on('proxyRes', (proxyRes, req, _res) => {});
    },
  },
];
