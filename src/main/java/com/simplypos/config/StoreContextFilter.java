package com.simplypos.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.simplypos.entities.Store;
import com.simplypos.entities.User;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter untuk menentukan store context aktif.
 *
 * Prioritas penentuan storeId:
 * 1. Header X-Store-Id (untuk SUPER_ADMIN switch cabang, atau API mobile)
 * 2. user.store_id dari user yang login (untuk ADMIN/STAFF/CASHIER yang terikat
 * cabang)
 * 3. null (biarkan kosong) — untuk SUPER_ADMIN yang mengakses lintas cabang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreContextFilter extends OncePerRequestFilter {

    private final StoreContext storeContext;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Value("${store.header:X-Store-Id}")
    private String storeHeader;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Lewati filter untuk endpoint publik
        if (path.contains("/auth/login")
                || path.contains("/actuator")
                || path.contains("/login")
                || path.contains("/logout")
                || path.contains("/locale")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        Long resolvedStoreId = null;

        try {
            // ============ PRIORITAS 1: Header X-Store-Id ============
            String storeIdHeader = request.getHeader(storeHeader);
            if (storeIdHeader != null && !storeIdHeader.isEmpty()) {
                try {
                    Long headerStoreId = Long.parseLong(storeIdHeader);
                    Store store = storeRepository.findById(headerStoreId).orElse(null);
                    if (store != null) {
                        resolvedStoreId = headerStoreId;
                        log.debug("[StoreContext] Resolved from header: {}", resolvedStoreId);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.setContentType("application/json");
                        response.getWriter().write(
                                "{\"error\":\"Store not found with id: " + headerStoreId + "\"}");
                        return;
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\":\"Invalid Store ID format\"}");
                    return;
                }
            }

            // ============ PRIORITAS 2: user.store_id dari user login ============
            if (resolvedStoreId == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth != null
                        && auth.isAuthenticated()
                        && !(auth instanceof AnonymousAuthenticationToken)) {

                    String username = auth.getName();
                    User user = userRepository.findByUsername(username).orElse(null);

                    if (user != null && user.getStore() != null) {
                        resolvedStoreId = user.getStore().getId();
                        log.debug("[StoreContext] Auto-resolved from user '{}': storeId={}",
                                username, resolvedStoreId);
                    } else {
                        log.debug("[StoreContext] User '{}' has no store binding (likely SUPER_ADMIN)",
                                username);
                    }
                }
            }

            // ============ Terapkan ke StoreContext ============
            if (resolvedStoreId != null) {
                storeContext.setCurrentStoreId(resolvedStoreId);
            } else {
                storeContext.clear();
            }

            filterChain.doFilter(request, response);

        } finally {
            // Selalu bersihkan context agar tidak bocor ke request lain (thread pool)
            storeContext.clear();
        }
    }
}