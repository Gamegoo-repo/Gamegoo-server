package com.gamegoo.jwt.util;

import com.gamegoo.dto.member.CustomMemberDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomMemberDetails memberDetails) {
            return memberDetails.getId();
        }

        return null; // or throw an exception if user is not authenticated
    }
}
