// ============================================================
// Business Central 365 Style – Simply PoS
// ============================================================
document.addEventListener('DOMContentLoaded', function () {

    // Toggle sidebar
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('bcSidebar');
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function () {
            sidebar.classList.toggle('collapsed');
            // localStorage.setItem('bc_sidebar_collapsed',
            //     sidebar.classList.contains('collapsed') ? '1' : '0');

            if (localStorage.getItem('bc_sidebar_collapsed') === '1') {
                sidebar.classList.add('collapsed');
            } else {
                sidebar.classList.remove('collapsed')
                localStorage.setItem('bc_sidebar_collapsed', '0');
            }
        });
        // Restore state
        if (localStorage.getItem('bc_sidebar_collapsed') === '1') {
            sidebar.classList.add('collapsed');
        }
    }

    // Auto-dismiss alerts after 5s
    document.querySelectorAll('.bc-alert').forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity .3s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });

    // Sidebar submenu collapse
    document.querySelectorAll('.bc-sidebar-link[data-bs-toggle="collapse"]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            e.preventDefault();
        });
    });

    // Confirm delete globally
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!confirm(form.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });

    // Format number inputs on blur (optional)
    document.querySelectorAll('input[type="number"]').forEach(function (input) {
        input.addEventListener('wheel', function (e) {
            if (document.activeElement === input) e.preventDefault();
        }, { passive: false });
    });

    // Highlight active menu
    const currentPath = window.location.pathname;
    document.querySelectorAll('.bc-sidebar-link').forEach(function (link) {
        const href = link.getAttribute('href');
        if (href && href !== '/' && currentPath.startsWith(href)) {
            link.classList.add('active');
        }
    });
});

// ============================================================
// Business Central 365 Style – Simply PoS
// Responsive + Mobile Friendly (FIXED)
// ============================================================
document.addEventListener('DOMContentLoaded', function () {

    // ============ DETEKSI MOBILE ============
    const isMobile = () => window.innerWidth <= 768;

    // ============ ELEMEN ============
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('bcSidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    const sidebarClose = document.getElementById('sidebarClose');

    // ============ GUARD: cegah handler terpasang 2x ============
    if (sidebar && sidebar.dataset.bc365Init === '1') {
        console.warn('[bc365] Sidebar handler sudah terpasang, skip init ulang.');
        return;
    }
    if (sidebar) sidebar.dataset.bc365Init = '1';

    // ============ SIDEBAR OPEN/CLOSE (dengan flag) ============
    let sidebarLockUntil = 0;   // cooldown ms untuk mencegah race condition

    function isLocked() {
        return Date.now() < sidebarLockUntil;
    }

    function lockSidebar(ms) {
        sidebarLockUntil = Date.now() + ms;
    }

    function openSidebar() {
        if (!sidebar || isLocked()) return;
        if (sidebar.classList.contains('mobile-open')) return;

        // ✅ Hapus class 'collapsed' agar tidak bentrok dengan 'mobile-open'
        sidebar.classList.remove('collapsed');

        sidebar.classList.add('mobile-open');

        if (sidebarOverlay) {
            sidebarOverlay.classList.add('active');
        }

        // ✅ Gunakan 'overflow-x: hidden' untuk minimize resize trigger di iOS
        //    (overflow: hidden pada body memicu resize event di iOS Safari)
        document.documentElement.style.overflowX = 'hidden';
        document.body.style.overflowX = 'hidden';

        lockSidebar(400);   // cegah double-toggle selama animasi
    }

    function closeSidebar() {
        if (!sidebar) return;

        sidebar.classList.remove('mobile-open');

        if (sidebarOverlay) {
            sidebarOverlay.classList.remove('active');
        }

        document.documentElement.style.overflowX = '';
        document.body.style.overflowX = '';

        lockSidebar(200);
    }

    // ============ HANDLER: TOGGLE BUTTON ============
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            e.preventDefault();

            // ✅ Abaikan klik jika masih dalam cooldown (mencegah double-fire)
            if (isLocked()) return;

            if (isMobile()) {
                // Mobile: toggle drawer
                if (sidebar.classList.contains('mobile-open')) {
                    closeSidebar();
                } else {
                    openSidebar();
                }
            } else {
                // Desktop: toggle collapse
                sidebar.classList.toggle('collapsed');
                localStorage.setItem(
                    'bc_sidebar_collapsed',
                    sidebar.classList.contains('collapsed') ? '1' : '0'
                );
            }
        }, { passive: false });

        // Restore collapsed state (desktop only, TANPA touch mobile)
        if (!isMobile() && localStorage.getItem('bc_sidebar_collapsed') === '1') {
            sidebar.classList.add('collapsed');
        }
    }

    // ============ HANDLER: CLOSE BUTTON ============
    if (sidebarClose) {
        sidebarClose.addEventListener('click', function (e) {
            e.stopPropagation();
            closeSidebar();
        });
    }

    // ============ HANDLER: OVERLAY CLICK ============
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', function (e) {
            e.stopPropagation();
            closeSidebar();
        });
    }

    // ============ HANDLER: ESC KEY ============
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && sidebar?.classList.contains('mobile-open')) {
            closeSidebar();
        }
    });

    // ============ HANDLER: RESIZE (DEBOUNCED, SAFE) ============
    // ✅ HANYA tutup sidebar jika berpindah dari mobile ke desktop.
    //    JANGAN tutup jika masih di mobile.
    let lastIsMobile = isMobile();
    let resizeTimer;

    window.addEventListener('resize', function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function () {
            const nowMobile = isMobile();

            // Hanya trigger jika status mobile berubah (dari mobile → desktop)
            if (lastIsMobile && !nowMobile) {
                // Beralih ke desktop → tutup drawer mobile
                if (sidebar?.classList.contains('mobile-open')) {
                    closeSidebar();
                }
            }

            lastIsMobile = nowMobile;
        }, 250);
    });

    // ============ HANDLER: SIDEBAR LINK CLICK ============
    document.querySelectorAll('.bc-sidebar-link').forEach(function (link) {
        link.addEventListener('click', function () {
            // Tutup sidebar mobile saat link diklik
            if (isMobile() && !link.hasAttribute('data-bs-toggle')) {
                closeSidebar();
            }
        });
    });

    // ============ AUTO-DISMISS ALERTS ============
    document.querySelectorAll('.bc-alert').forEach(function (alert) {
        if (alert.dataset.dismissible === 'false') return;
        setTimeout(function () {
            alert.style.transition = 'opacity .3s, transform .3s';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });

    // ============ CONFIRM DELETE ============
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!confirm(form.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });

    // ============ NUMBER INPUT SCROLL GUARD ============
    document.querySelectorAll('input[type="number"]').forEach(function (input) {
        input.addEventListener('wheel', function (e) {
            if (document.activeElement === input) e.preventDefault();
        }, { passive: false });
    });

    // ============ AUTO-HIGHLIGHT ACTIVE MENU ============
    const currentPath = window.location.pathname;
    document.querySelectorAll('.bc-sidebar-link').forEach(function (link) {
        const href = link.getAttribute('href');
        if (href && href !== '/' && currentPath.startsWith(href)) {
            link.classList.add('active');
        }
    });

    // ============ RESPONSIVE TABLES ============
    document.querySelectorAll('.bc-card > table.bc-table').forEach(function (table) {
        if (table.parentElement.classList.contains('bc-table-wrapper')) return;
        const wrapper = document.createElement('div');
        wrapper.className = 'bc-table-wrapper';
        table.parentNode.insertBefore(wrapper, table);
        wrapper.appendChild(table);
    });

    // ============ DROPDOWN AUTO-CLOSE ============
    document.addEventListener('click', function (e) {
        const openDropdowns = document.querySelectorAll('.dropdown-menu.show');
        if (openDropdowns.length > 0) {
            const clickedInsideDropdown = e.target.closest('.dropdown');
            if (!clickedInsideDropdown) {
                openDropdowns.forEach(d => {
                    const toggle = d.parentElement.querySelector('[data-bs-toggle="dropdown"]');
                    if (toggle) {
                        const bsDropdown = bootstrap.Dropdown.getInstance(toggle);
                        if (bsDropdown) bsDropdown.hide();
                    }
                });
            }
        }
    });

    // ============ SCROLL RESTORATION ============
    if ('scrollRestoration' in history) {
        history.scrollRestoration = 'manual';
    }

    // ============ DEBUG INFO ============
    console.log('[bc365] Initialized. isMobile=' + isMobile()
        + ', sidebarToggle=' + !!sidebarToggle);
});

// ============================================================
// GLOBAL HELPERS
// ============================================================
window.isMobileView = function () {
    return window.innerWidth <= 768;
};

window.isTabletView = function () {
    return window.innerWidth <= 1024 && window.innerWidth > 768;
};

// ============================================================
// i18n Helper
// ============================================================
window.I18N = window.I18N || {};

window.t = function (key, fallback) {
    return window.I18N[key] || fallback || key;
};

// Auto-load translations pada DOM ready
document.addEventListener('DOMContentLoaded', function () {
    if (Object.keys(window.I18N).length === 0) {
        fetch('/api/i18n', { credentials: 'same-origin' })
            .then(r => r.ok ? r.json() : {})
            .then(msgs => Object.assign(window.I18N, msgs))
            .catch(() => { });
    }
});

// ============================================================
// THEME TOGGLE (Dark/Light Mode)
// ============================================================
(function initThemeToggle() {
    const html = document.documentElement;
    const themeToggle = document.getElementById('themeToggle');
    const iconLight = document.querySelector('.theme-icon-light');
    const iconDark = document.querySelector('.theme-icon-dark');

    if (!themeToggle) return;

    // Guard agar tidak double-init
    if (themeToggle.dataset.themeInit === '1') return;
    themeToggle.dataset.themeInit = '1';

    function getCurrentTheme() {
        return html.getAttribute('data-theme') || 'light';
    }

    function updateIcons() {
        const theme = getCurrentTheme();
        // Light mode → tampilkan moon (klik untuk dark)
        // Dark mode → tampilkan sun (klik untuk light)
        if (iconLight) iconLight.style.display = theme === 'dark' ? 'inline-block' : 'none';
        if (iconDark) iconDark.style.display = theme === 'light' ? 'inline-block' : 'none';

        // Update title
        themeToggle.title = theme === 'dark'
            ? 'Switch to Light Mode'
            : 'Switch to Dark Mode';
    }

    function setTheme(theme) {
        // Enable transition sementara
        html.classList.add('theme-transition');

        html.setAttribute('data-theme', theme);
        try {
            localStorage.setItem('bc-theme', theme);
        } catch (e) {
            console.warn('[theme] Cannot save theme preference:', e);
        }

        // Update meta theme-color
        const metaThemeColor = document.querySelector('meta[name="theme-color"]');
        if (metaThemeColor) {
            metaThemeColor.setAttribute('content',
                theme === 'dark' ? '#1B1A19' : '#0078D4');
        }

        updateIcons();

        // Remove transition class setelah animasi selesai
        setTimeout(() => html.classList.remove('theme-transition'), 300);
    }

    function toggleTheme() {
        const current = getCurrentTheme();
        setTheme(current === 'dark' ? 'light' : 'dark');
    }

    // Bind click
    themeToggle.addEventListener('click', function (e) {
        e.stopPropagation();
        toggleTheme();
    });

    // Init icons berdasarkan theme saat ini
    updateIcons();

    // Listen to system preference change (hanya jika user belum pilih manual)
    if (window.matchMedia) {
        const darkQuery = window.matchMedia('(prefers-color-scheme: dark)');
        darkQuery.addEventListener('change', function (e) {
            // Hanya auto-switch jika user belum pernah set manual
            const savedTheme = localStorage.getItem('bc-theme');
            if (!savedTheme) {
                setTheme(e.matches ? 'dark' : 'light');
            }
        });
    }
})();