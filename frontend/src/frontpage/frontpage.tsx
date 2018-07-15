import * as React from "react";
import userManager from "../auth/userManager";
import { MouseEvent } from "react";

export class Frontpage extends React.Component {
    private onLoginButtonClick(event: MouseEvent<HTMLButtonElement>) {
        event.preventDefault();
        userManager.signinRedirect();
    }

    public render() {
        return <div>
            <h1>Mealfu</h1>
            <button onClick={this.onLoginButtonClick}>Login with Google</button>
        </div>;
    }
}
