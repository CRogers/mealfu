const path = require('path');
const fs = require('fs');
const process = require('process');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackInlineSourcePlugin = require('html-webpack-inline-source-plugin');
const webpack = require('webpack');
const history = require('connect-history-api-fallback');
const convert = require('koa-connect');

const siteDir = path.resolve(__dirname, 'site');

const localServerPort = fs.readFileSync(__dirname + '/../local-server-port', { encoding: 'utf8' }).trim();

module.exports = (env, originalArgv) => {
    const argv = originalArgv || {};
    const mode = argv.mode || (process.env.WEBPACK_SERVE && 'development') || 'production';
    console.log('In mode', mode);

    return {
        mode,
        entry: {
            'mealfu': './src/index.tsx',
            '404': './src/github-pages/gh-pages-spa-redirect.ts',
        },
        module: {
            rules: [
                {
                    test: /\.tsx?$/,
                    use: 'ts-loader',
                    exclude: /node_modules/
                }
            ]
        },
        resolve: {
            extensions: ['.tsx', '.ts', '.js']
        },
        output: {
            filename: '[name].js',
            path: siteDir,
        },
        plugins: [
            new HtmlWebpackPlugin({
                title: 'Mealfu',
                chunks: ['mealfu'],
            }),
            new HtmlWebpackPlugin({
                title: 'Mealfu Github Pages SPA Redirect',
                chunks: ['404'],
                filename: '404.html',
                inlineSource: '.js$'
            }),
            new HtmlWebpackInlineSourcePlugin(),
            new webpack.DefinePlugin({
                WEBPACK_DEFINED_API_URL_BASE: JSON.stringify(mode === 'production'
                    ? 'https://q6zvj19zu3.execute-api.eu-west-2.amazonaws.com/dev'
                    : 'http://localhost:' + localServerPort),
                WEBPACK_DEFINED_BROWSER_URL_BASENAME: JSON.stringify(mode === 'production'
                    ? '/mealfu-frontend/'
                    : '/')
            })
        ],
        serve: {
            open: true,
            hot: true,
            add: (app, middleware, options) => {
                const historyOptions = {
                    index: '/',
                    rewrites: [
                        { from: /^\/[^.]+$/, to: '/404.html' }
                    ]
                };

                app.use(convert(history(historyOptions)));
            },
        }
    };
}