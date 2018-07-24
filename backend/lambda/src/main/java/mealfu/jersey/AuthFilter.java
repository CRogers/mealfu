package mealfu.jersey;

import mealfu.auth.AuthHeader;
import mealfu.auth.AuthHeaderVerificationException;
import mealfu.auth.UserAuthorizer;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.Optional;

public class AuthFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";

    private final UserAuthorizer userAuthorizer;

    public AuthFilter(UserAuthorizer userAuthorizer) {
        this.userAuthorizer = userAuthorizer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }

        String authHeaderString = Optional.ofNullable(requestContext.getHeaderString(AUTHORIZATION))
                .orElseThrow(() -> new NotAuthorizedException(AUTHORIZATION + " header param is required"));

        try {
            userAuthorizer.verifyUser(AuthHeader.fromString(authHeaderString));
        } catch (AuthHeaderVerificationException e) {
            throw new NotAuthorizedException(e);
        }
    }
}
