package com.sanedge.reditclone.security;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sanedge.reditclone.services.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserService userService;

  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      Optional<String> accessToken = parseJwt(request);
      if (accessToken.isPresent() && jwtUtils.validateAccessToken(accessToken.get())) {
        String username = jwtUtils.getUsernameAccessToken(accessToken.get());
        UserDetails userDetails = userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

      }

    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e);
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> parseJwt(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return Optional.of(authHeader.replace("Bearer ", ""));
    }
    return Optional.empty();
  }
}
