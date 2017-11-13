var commonConfig = require('./webpack.common.config.js');
var webpack = require('webpack');
var merge = require('webpack-merge');

module.exports = function(env) {
    var devConfig = {
        devtool: 'inline-source-map',
        devServer: {
            contentBase: './dist',
            stats: 'minimal',
            compress: true,
            open: true,
            hot: true,
            port: 9000,
            watchOptions: {
                ignored: /node_modules/,
                aggregateTimeout: 1000
            }
        },
        plugins: [
            new webpack.NamedModulesPlugin(),
            new webpack.HotModuleReplacementPlugin()
        ]
    };

    if (env.enableStyleCheck) {
        devConfig.module = {
            rules: [
                {
                    test: /\.js$/,
                    exclude: /node_modules/,
                    loader: 'eslint-loader'
                }
            ]
        };
    }

    return merge(commonConfig(env), devConfig);
};
