package com.barclays.takehomecodingtest.utils;

import com.barclays.takehomecodingtest.dto.UserResponse;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.service.UnauthorizedAccessException;

public class UserHelper {
    public static UserResponse convertUserEntityToUserResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getName(),
                user.getPhoneNumber(), user.getEmail(),
                new com.barclays.takehomecodingtest.dto.Address(user.getAddress().getLine1(),
                        user.getAddress().getLine2(),
                        user.getAddress().getLine3(),
                        user.getAddress().getTown(), user.getAddress().getCounty(), user.getAddress().getPostcode()),
                user.getCreatedAt(), user.getUpdatedAt());
    }

    public static void isUserAuthorisedForAccount(AccountEntity account,
            String authenticatedUserId) {
        if (!account.getUser().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException(
                    "User is not authorized to access account " + account.getAccountNumber());
        }
    }
}
