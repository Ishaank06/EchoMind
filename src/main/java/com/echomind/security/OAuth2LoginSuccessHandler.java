package com.echomind.security;

import com.echomind.entity.Role;
import com.echomind.entity.User;
import com.echomind.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles the callback after successful GitHub OAuth2 login.
 *
 * The OAuth2 flow (what happens behind the scenes):
 *
 * 1. User clicks "Login with GitHub"
 *    → Browser redirects to: github.com/login/oauth/authorize?client_id=...
 *
 * 2. User authorizes EchoMind on GitHub
 *    → GitHub redirects back to: localhost:8080/login/oauth2/code/github?code=ABC123
 *
 * 3. Spring Security (OAuth2 Client):
 *    - Exchanges the code for an access token (server-to-server, invisible to user)
 *    - Uses the access token to fetch the user's GitHub profile
 *    - Creates an OAuth2User object with the profile data
 *    - Calls THIS handler with the OAuth2User
 *
 * 4. THIS handler:
 *    - Extracts email/name from GitHub profile
 *    - Finds or creates a User in our database
 *    - Generates an EchoMind JWT (our own token, NOT GitHub's token)
 *    - Redirects with the JWT
 *
 * Why issue our own JWT instead of using GitHub's access token?
 * - GitHub's token is for accessing GitHub's API, not ours
 * - GitHub's token has GitHub's expiration policy, not ours
 * - Our JWT contains OUR claims (role, user ID in our DB)
 * - If we later add Google OAuth, we'd have tokens from different providers
 *   with different formats — our JWT normalizes everything
 * - The frontend only needs to handle one token format
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // GitHub returns these attributes in the user profile
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String githubId = oAuth2User.getAttribute("id").toString();

        // Fallback: GitHub allows users to hide their email.
        // The "login" attribute (GitHub username) is always available.
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.user";
        }
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }

        // Find existing user or create new one
        final String finalEmail = email;
        final String finalName = name;
        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .name(finalName)
                            .email(finalEmail)
                            .role(Role.ROLE_USER)
                            .provider("github")
                            .providerId(githubId)
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate OUR JWT
        String token = jwtService.generateToken(user);

        // Redirect with token as query parameter.
        // In production with a React frontend, this would redirect to
        // something like: http://localhost:3000/oauth/callback?token=...
        // The frontend extracts the token and stores it.
        // For now (no frontend), we redirect to a simple endpoint.
        response.sendRedirect("/auth/oauth2/success?token=" + token);
    }
}
