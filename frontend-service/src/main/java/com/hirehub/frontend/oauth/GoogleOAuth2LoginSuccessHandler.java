package com.hirehub.frontend.oauth;

import com.hirehub.frontend.notification.AuthNotificationService;
import com.hirehub.frontend.auth.FrontendJwtService;
import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.auth.SessionAuthSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class GoogleOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuthAccountService googleOAuthAccountService;
    private final SecurityContextRepository securityContextRepository;
    private final FrontendJwtService frontendJwtService;
    private final AuthNotificationService authNotificationService;

    public GoogleOAuth2LoginSuccessHandler(
            GoogleOAuthAccountService googleOAuthAccountService,
            SecurityContextRepository securityContextRepository,
            FrontendJwtService frontendJwtService,
            AuthNotificationService authNotificationService
    ) {
        this.googleOAuthAccountService = googleOAuthAccountService;
        this.securityContextRepository = securityContextRepository;
        this.frontendJwtService = frontendJwtService;
        this.authNotificationService = authNotificationService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            response.sendRedirect(request.getContextPath() + "/login?oauth_error=1");
            return;
        }
        OAuth2User principal = oauth2Token.getPrincipal();
        String email = principal.getAttribute("email");
        if (!StringUtils.hasText(email)) {
            response.sendRedirect(request.getContextPath() + "/login?oauth_error=1");
            return;
        }
        Object verified = principal.getAttribute("email_verified");
        if (verified instanceof Boolean b && !b) {
            response.sendRedirect(request.getContextPath() + "/login?oauth_error=1");
            return;
        }
        HirehubUserDetails userDetails;
        try {
            userDetails = googleOAuthAccountService.loadExistingFromGoogle(email);
        } catch (GoogleOAuthAccountNotRegisteredException ex) {
            response.sendRedirect(request.getContextPath() + "/login?oauth_error=not_registered");
            return;
        }
        if (!userDetails.isEnabled()) {
            response.sendRedirect(request.getContextPath() + "/login?error=blocked");
            return;
        }

        UsernamePasswordAuthenticationToken sessionAuth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(sessionAuth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        SessionAuthSupport.storeAccessToken(frontendJwtService.generateAccessToken(userDetails));

        try {
            authNotificationService.publishLogin(userDetails, request);
        } catch (Exception ignored) {
            // Notification best-effort
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}
