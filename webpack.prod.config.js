var commonConfig = require('./webpack.common.config.js');
var UglifyJSPlugin = require('uglifyjs-webpack-plugin');
var merge = require('webpack-merge');

module.exports = function(env) {
    var prodConfig = {
        devtool: false,
        plugins: [
            new UglifyJSPlugin({
                test: /\.js($|\?)/i,
                uglifyOptions: {
                    ecma: 5,
                    mangle: false
                }
            })
        ]
    };

    return merge(commonConfig(env), prodConfig);
};
