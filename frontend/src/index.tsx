import * as ReactDom from 'react-dom';
import * as React from 'react';
import { applyMiddleware, createStore } from 'redux';
import { Provider } from "react-redux";
import { ConnectedRouter, connectRouter, routerMiddleware } from 'connected-react-router'
import createBrowserHistory from "history/createBrowserHistory";
import { Route, Switch } from "react-router";
import { Frontpage } from "./frontpage/frontpage";

declare const API_URL_BASE: string;

const reducer = (state:  string) => state;

const history = createBrowserHistory();

const store = createStore(
    connectRouter(history)(reducer),
    'hi',
    applyMiddleware(
        routerMiddleware(history)
    )
);

const appContainer = document.createElement('div');
document.body.appendChild(appContainer)

ReactDom.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <Switch>
                <Route exact path="/" render={() => <Frontpage />}/>
                <Route render={() => (<div>where?</div>)} />
            </Switch>
        </ConnectedRouter>
    </Provider>,
    appContainer);

fetch(API_URL_BASE + '/test', {
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