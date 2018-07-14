import * as ReactDom from 'react-dom';
import * as React from 'react';

declare const URL_BASE: string;

const appContainer = document.createElement('div');
document.body.appendChild(appContainer)

ReactDom.render(<h1>Hello, world!</h1>, appContainer);

const time = fetch(URL_BASE + '/test', {
    method: 'GET',
})
    .then(response => {
        return response.text();})
    .then(text => {
        console.log(text);
        const newChild = document.createElement('div');
        newChild.textContent = text;

        document.body.appendChild(newChild);
    });