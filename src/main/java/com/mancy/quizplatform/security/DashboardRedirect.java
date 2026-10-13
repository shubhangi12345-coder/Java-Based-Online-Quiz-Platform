package com.mancy.quizplatform.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/** Decides which dashboard belongs to the logged-in user. */
public final class DashboardRedirect {

    private DashboardRedirect() {
    }

    /** True only for a really logged-in user (not the anonymous visitor). */
    public static boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static String pathFor(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return "/admin/dashboard";
            }
            if ("ROLE_CREATOR".equals(authority.getAuthority())) {
                return "/creator/dashboard";
            }
        }
        return "/participant/dashboard";
    }
}
