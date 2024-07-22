var commonConfig = require('./webpack.common.config.js');
var UglifyJSPlugin = require('uglifyjs-webpack-plugin');
var merge = require('webpack-merge');
var webpack = require('webpack');
var fs = require('fs');

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
            }),
            new webpack.BannerPlugin({
                banner: fs.readFileSync('./copyright.ipr', 'utf8'),
                include: /app*/,
                entryOnly: true
            })
        ]
    };

    return merge(commonConfig(env), prodConfig);
};
