package com.ust.lms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides utility methods for accessing the current user's security context
 * and checking user roles.
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Retrieves the email address of the currently authenticated user.
     *
     * @return current user's email address, or "SYSTEM" if no authenticated user is available
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "SYSTEM";
        }
        return authentication.getName();
    }

    /**
     * Checks whether the currently authenticated user has any of the specified roles.
     *
     * @param roles roles to check against the current user's authorities
     * @return true if the user has at least one of the specified roles; false otherwise
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (String role : roles) {
            boolean match = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
            if (match) {
                return true;
            }
        }
        return false;
    }
}