var path = require('path');
var webpackConf = require('./webpack.common.config');

module.exports = function(config) {
    config.set({
        basePath: '',
        browsers: [config.browser || 'PhantomJS'],
        phantomjsLauncher: {
            // Have phantomjs exit if a ResourceError is encountered (useful if karma exits without killing phantom)
            exitOnResourceError: true
        },
        frameworks: ['jasmine'],
        files: [
            './webpack/tests.webpack.js'
        ],
        preprocessors: {
            './webpack/tests.webpack.js': ['webpack']
        },
        webpack: webpackConf({build: 'test'}),
        reporters: ['progress', 'coverage-istanbul'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        singleRun: true,

        coverageIstanbulReporter: {
            reports: ['html', 'lcovonly', 'text-summary'],
            dir: path.join(__dirname, 'coverage'),
            fixWebpackSourcePaths: true,
            skipFilesWithNoCoverage: true,
            'report-config': {
                html: {
                    subdir: 'html'
                }

            }
        }
    });
};
