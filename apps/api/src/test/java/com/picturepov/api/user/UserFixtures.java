package com.picturepov.api.user;

/** A real CurrentUserService for unit tests in other packages; only its database and Cognito are replaced. */
public final class UserFixtures {

    private UserFixtures() {}

    public static CurrentUserService currentUserService(UserRepository userRepository, CognitoUserInfoClient userInfoClient) {
        return new CurrentUserService(userRepository, userInfoClient);
    }
}
