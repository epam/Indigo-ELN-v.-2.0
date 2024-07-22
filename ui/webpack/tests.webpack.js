require('angular');
require('angular-mocks/angular-mocks');

var srcContext = require.context('../src', true, /app\.module\.js$/);
srcContext.keys().forEach(srcContext);

var testSrcContext = require.context('../src', true, /\.spec.js$/);
testSrcContext.keys().forEach(testSrcContext);
