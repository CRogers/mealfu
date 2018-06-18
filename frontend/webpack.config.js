const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const siteDir = path.resolve(__dirname, 'site');

module.exports = {
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
        extensions: [ '.tsx', '.ts', '.js' ]
    },
    output: {
        filename: 'mealfu.js',
        path: siteDir,
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Mealfu'
        })
    ],
    serve: {
        open: true,
        hot: true
    }
};