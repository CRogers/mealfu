import { createUserManager } from 'redux-oidc';
import { BROWSER_URL_BASENAME } from "../constants/buildConstants";

const userManagerConfig = {
    client_id: '304581327654-kiauhniedmfphqua8rnlpsmhg9mcumq2.apps.googleusercontent.com',
    redirect_uri: `${window.location.protocol}//${window.location.hostname}${window.location.port ? `:${window.location.port}` : ''}${BROWSER_URL_BASENAME}oauth2-callback`,
    response_type: 'token id_token',
    scope: 'openid profile',
    authority: 'https://accounts.google.com',
    silent_redirect_uri: `${window.location.protocol}//${window.location.hostname}${window.location.port ? `:${window.location.port}` : ''}/silent_renew.html`,
    automaticSilentRenew: false,
    filterProtocolClaims: true,
    loadUserInfo: true,
};

const userManager = createUserManager(userManagerConfig);

(window as any).um = userManager;

export default userManager;
