var commonConfig = require('./webpack.common.config.js');
var UglifyJSPlugin = require('uglifyjs-webpack-plugin');
var merge = require('webpack-merge');

module.exports = function(env) {
    return merge(commonConfig(env), {
        // plugins: [
        //     new UglifyJSPlugin()
        // ]
    });
};
