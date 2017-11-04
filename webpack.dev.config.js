var commonConfig = require('./webpack.common.config.js');
var webpack = require('webpack');
var merge = require('webpack-merge');

module.exports = function(env) {
    return merge(commonConfig(env), {
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
        //TODO Enable
        // module: {
        //     rules: [
        //         {
        //             test: /\.js$/,
        //             exclude: /node_modules/,
        //             loader: 'eslint-loader'
        //         }
        //     ]
        // }
    });
};
