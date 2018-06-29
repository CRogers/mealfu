const path = require('path');
const process = require('process');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');

const siteDir = path.resolve(__dirname, 'site');

module.exports = (env, originalArgv) => {
    const argv = originalArgv || {};
    const mode = argv.mode || (process.env.WEBPACK_SERVE && 'development') || 'production';
    console.log('In mode', mode);

    return {
        mode,
        entry: './src/index.ts',
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
            filename: 'mealfu.js',
            path: siteDir,
        },
        plugins: [
            new HtmlWebpackPlugin({
                title: 'Mealfu'
            }),
            new webpack.DefinePlugin({
                URL_BASE: JSON.stringify(mode === 'production'
                    ? 'https://q6zvj19zu3.execute-api.eu-west-2.amazonaws.com/dev/'
                    : 'http://localhost:9876')
            })
        ],
        serve: {
            open: true,
            hot: true
        }
    };
}