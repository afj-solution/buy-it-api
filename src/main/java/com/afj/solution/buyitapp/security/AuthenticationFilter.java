package com.afj.solution.buyitapp.security;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.afj.solution.buyitapp.constans.Redirects;
import com.afj.solution.buyitapp.service.AppUserDetailsService;

/**
 * @author Tomash Gombosh
 */
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtProvider;

    @Autowired
    private AppUserDetailsService service;

    @Autowired
    private Redirects redirects;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final String jwt = jwtProvider.getJwtFromRequest(request);
        if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
            final Collection<? extends GrantedAuthority> roles = jwtProvider.getRoleFromToken(jwt)
                    .stream()
                    .map(r -> new SimpleGrantedAuthority(r.get("authority")))
                    .toList();
            final UUID id = jwtProvider.getUuidFromToken(jwt);
            final UserDetails userDetails = service.loadUserById(id);
            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                final String redirectUrl = !userDetails.isAccountNonLocked()
                        ? redirects.getUserLockedUrl()
                        : redirects.getUserDisabledUrl();
                response.setStatus(302);
                response.sendRedirect(redirectUrl);
            }
            final AbstractAuthenticationToken authentication
                    = getAuth(roles, id, userDetails);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Do internal filtering of the request for {}(enabled -> {}, locked -> {})",
                    id, userDetails.isEnabled(), userDetails.isAccountNonLocked());
        }
        filterChain.doFilter(request, response);
    }

    private AbstractAuthenticationToken getAuth(final Collection<? extends GrantedAuthority> roles,
                                                final UUID id,
                                                final UserDetails userDetails) {
        if (roles.stream().anyMatch(r -> "ROLE_ANONYMOUS".equals(r.getAuthority()))) {
            log.info("User {} is anonymous", id);
        }
        log.info("User {} is authenticated", id);
        return new UsernamePasswordAuthenticationToken(userDetails,
                id,
                roles);
    }
}
