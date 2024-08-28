var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var CleanWebpackPlugin = require('clean-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');
var autoprefixer = require('autoprefixer');
var _ = require('lodash');

var copy = require('./webpack/copy');

module.exports = function(env) {
    var IS_PROD = env.build === 'prod';
    var IS_DEV = env.build === 'dev';
    var IS_TEST = env.build === 'test';

    var apiUrl = _.isArray(env.apiUrl) ? _.last(env.apiUrl) : env.apiUrl;

    var DIRS = {
        app: path.join(__dirname, 'src', 'app'),
        src: path.join(__dirname, 'src'),
        assets: path.join(__dirname, 'src', 'assets'),
        dist: path.join(__dirname, 'dist')
    };

    //TODO: delete babel loader when phantomjs 2.5 will be available
    var commonConfig = {
        entry: {
            app: path.join(DIRS.app, 'app.module.js'),
            vendors: path.join(DIRS.app, 'dependencies/vendors.js')
        },
        plugins: [
            new CleanWebpackPlugin([DIRS.dist]),
            new webpack.DefinePlugin({
                apiUrl: JSON.stringify(apiUrl)
            }),
            new CopyWebpackPlugin(copy(DIRS))
        ],
        output: {
            filename: '[name].bundle.js',
            path: DIRS.dist
        },
        devtool: 'inline-source-map',
        module: {
            rules: [
                {
                    test: /\.js$/,
                    include: path.resolve(__dirname, 'node_modules/pretty-bytes'),
                    use: {
                        loader: 'babel-loader',
                        options: {
                            presets: ['es2015']
                        }
                    }
                },
                {
                    test: /\.js$/,
                    exclude: /(node_modules)/,
                    loader: 'ng-annotate-loader'
                },
                {
                    test: /\.css$/,
                    loader: IS_TEST ? 'null-loader' : ExtractTextPlugin.extract({
                        fallback: 'style-loader',
                        use: [
                            {
                                loader: 'css-loader',
                                options: {
                                    minimize: IS_PROD
                                }
                            }
                        ]
                    })
                },
                {
                    test: /\.less$/,
                    loader: IS_TEST ? 'null-loader' : ExtractTextPlugin.extract({
                        fallback: 'style-loader',
                        use: [
                            {
                                loader: 'css-loader',
                                options: {sourceMap: IS_DEV, minimize: IS_PROD}
                            },
                            {
                                loader: 'postcss-loader',
                                options: {
                                    plugins: function() {
                                        return [autoprefixer()];
                                    },
                                    sourceMap: IS_DEV
                                }
                            },
                            {
                                loader: 'less-loader',
                                options: {sourceMap: IS_DEV}
                            }
                        ]
                    })
                },
                {
                    test: /\.(png|svg|jpg|gif)$/,
                    loader: 'file-loader'
                },
                {
                    test: /\.(woff|woff2|eot|ttf|otf)$/,
                    loader: 'file-loader'
                },
                {
                    test: /\.(html)$/,
                    use: {
                        loader: 'html-loader',
                        options: {
                            minimize: IS_PROD,
                            removeComments: IS_PROD,
                            attrs: ['img:src']
                        }
                    }
                }
            ]
        }
    };

    if (!IS_TEST) {
        commonConfig.plugins.push(
            new webpack.optimize.CommonsChunkPlugin({
                    name: 'vendors',
                    minChunks: Infinity
                }
            ),
            new ExtractTextPlugin({filename: '[name].bundle.css', allChunks: true}),
            new HtmlWebpackPlugin({
                    favicon: path.join(DIRS.assets, 'images', 'favicon.ico'),
                    template: path.join(DIRS.src, 'index.html'),
                    filename: 'index.html',
                    hash: true,
                    chunks: ['vendors', 'app']
                }
            )
        );
    }

    if (IS_TEST) {
        commonConfig.module.rules.push(
            {
                test: /\.js$/,
                enforce: 'post',
                exclude: [
                    /(node_modules|webpack|dependencies)/,
                    /\.(spec|constant|config|module|run)\.js$/
                ],
                loader: 'istanbul-instrumenter-loader'
            }
        );
    }

    return commonConfig;
};
