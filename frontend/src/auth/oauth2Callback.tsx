import { CallbackComponent } from "redux-oidc";
import { push } from "connected-react-router";
import userManager from "./userManager";
import * as React from "react";
import { connect, DispatchProp } from "react-redux";

const FixedCallbackComponent = CallbackComponent as any;

interface CallbackPageProps extends DispatchProp {}

class CallbackPageInternal extends React.Component<CallbackPageProps, {}> {
    render() {
        // just redirect to '/' in both cases
        return (
            <FixedCallbackComponent
                userManager={userManager}
                successCallback={() => this.props.dispatch(push("/"))}
                errorCallback={(error: any) => {
                    this.props.dispatch(push("/"));
                    console.error(error);
                }}
            >
                <div>Redirecting...</div>
            </FixedCallbackComponent>
        );
    }
}

export const CallbackPage = connect()(CallbackPageInternal);