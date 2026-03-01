/* ═══════════════════════════════════════════════════════════
   SmartCM — DASHBOARD BASE SCRIPT
   Sidebar collapse · Mobile drawer · Profile dropdown
   Collapsed tooltips · Active nav highlight
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    const COLLAPSE_KEY = 'scm_sidebar_collapsed';

    /* ── shorthand ───────────────────────────────────────── */
    const el  = id  => document.getElementById(id);
    const qs  = sel => document.querySelector(sel);
    const qsa = sel => document.querySelectorAll(sel);

    /* ── elements ────────────────────────────────────────── */
    const sidebar      = el('dbSidebar');
    const dbMain       = el('dbMain');
    const collapseBtn  = el('dbCollapseBtn');
    const mobileBtn    = el('dbMobileBtn');
    const overlay      = el('dbOverlay');
    const profileBtn   = el('dbProfileBtn');
    const profileDrop  = el('dbProfileDropdown');

    /* ══════════════════════════════════════════════════════
       1. SIDEBAR — COLLAPSE / EXPAND (desktop)
       ══════════════════════════════════════════════════════ */
    function initCollapse() {
        if (!sidebar || !collapseBtn || !dbMain) return;

        // Restore persisted state
        if (localStorage.getItem(COLLAPSE_KEY) === 'true') {
            sidebar.classList.add('collapsed');
            dbMain.classList.add('sidebar-collapsed');
        }

        collapseBtn.addEventListener('click', () => {
            const isNowCollapsed = sidebar.classList.toggle('collapsed');
            dbMain.classList.toggle('sidebar-collapsed', isNowCollapsed);
            localStorage.setItem(COLLAPSE_KEY, isNowCollapsed);
        });
    }

    /* ══════════════════════════════════════════════════════
       2. SIDEBAR — MOBILE DRAWER
       ══════════════════════════════════════════════════════ */
    function initMobileDrawer() {
        if (!sidebar || !mobileBtn || !overlay) return;

        const openDrawer = () => {
            sidebar.classList.add('mobile-open');
            overlay.style.display = 'block';
            document.body.style.overflow = 'hidden';
        };

        const closeDrawer = () => {
            sidebar.classList.remove('mobile-open');
            overlay.style.display = 'none';
            document.body.style.overflow = '';
        };

        mobileBtn.addEventListener('click', openDrawer);
        overlay.addEventListener('click', closeDrawer);

        // Close when a nav link is tapped
        qsa('.db-nav a').forEach(link => {
            link.addEventListener('click', () => {
                if (window.innerWidth <= 768) closeDrawer();
            });
        });

        // Close on Escape
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') closeDrawer();
        });
    }

    /* ══════════════════════════════════════════════════════
       3. PROFILE DROPDOWN
       ══════════════════════════════════════════════════════ */
    function initProfileDropdown() {
        if (!profileBtn || !profileDrop) return;

        const open = () => {
            profileDrop.classList.add('open');
            profileBtn.classList.add('open');
            profileBtn.setAttribute('aria-expanded', 'true');
        };

        const close = () => {
            profileDrop.classList.remove('open');
            profileBtn.classList.remove('open');
            profileBtn.setAttribute('aria-expanded', 'false');
        };

        const toggle = () => {
            profileDrop.classList.contains('open') ? close() : open();
        };

        profileBtn.addEventListener('click', e => {
            e.stopPropagation();
            toggle();
        });

        // Close when clicking outside
        document.addEventListener('click', e => {
            if (!profileBtn.contains(e.target) && !profileDrop.contains(e.target)) {
                close();
            }
        });

        // Close on Escape
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') close();
        });
    }

    /* ══════════════════════════════════════════════════════
       4. COLLAPSED SIDEBAR TOOLTIPS
       Shows label as floating tooltip when sidebar is collapsed
       ══════════════════════════════════════════════════════ */
    function initCollapsedTooltips() {
        if (!sidebar) return;

        const TIP_ID = 'scm-nav-tip';

        const removeTip = () => {
            const old = el(TIP_ID);
            if (old) {
                old.style.opacity = '0';
                setTimeout(() => old && old.remove(), 150);
            }
        };

        qsa('.db-nav a').forEach(link => {
            const textEl = link.querySelector('.db-nav-text');
            if (!textEl) return;
            const label = textEl.textContent.trim();

            link.addEventListener('mouseenter', () => {
                if (!sidebar.classList.contains('collapsed')) return;
                removeTip();

                const tip = document.createElement('div');
                tip.id = TIP_ID;
                tip.textContent = label;

                Object.assign(tip.style, {
                    position:     'fixed',
                    background:   'rgba(13, 16, 24, 0.98)',
                    color:        '#eaedf5',
                    border:       '1px solid rgba(37, 99, 235, 0.32)',
                    borderRadius: '8px',
                    padding:      '6px 13px',
                    fontSize:     '0.80rem',
                    fontFamily:   "'Sora', system-ui, sans-serif",
                    fontWeight:   '400',
                    pointerEvents:'none',
                    zIndex:       '9999',
                    whiteSpace:   'nowrap',
                    boxShadow:    '0 6px 24px rgba(0,0,0,0.50)',
                    opacity:      '0',
                    transition:   'opacity 0.15s ease',
                    letterSpacing:'0.01em',
                });

                document.body.appendChild(tip);

                const rect = link.getBoundingClientRect();
                const tipH = tip.offsetHeight || 30;
                tip.style.top  = (rect.top + (rect.height - tipH) / 2) + 'px';
                tip.style.left = (rect.right + 10) + 'px';

                requestAnimationFrame(() => { tip.style.opacity = '1'; });
            });

            link.addEventListener('mouseleave', removeTip);
        });
    }

    /* ══════════════════════════════════════════════════════
       5. ACTIVE NAV — fallback for static preview
       (Thymeleaf handles this server-side via th:classappend)
       ══════════════════════════════════════════════════════ */
    function initActiveNav() {
        if (!sidebar) return;
        const path = window.location.pathname;

        qsa('.db-nav a').forEach(link => {
            if (link.classList.contains('db-active')) return; // already set by Thymeleaf
            const href = link.getAttribute('href');
            if (href && href !== '/' && path.startsWith(href)) {
                link.classList.add('db-active');
            }
        });
    }

    /* ══════════════════════════════════════════════════════
       6. RESIZE HANDLER
       Reset mobile state on resize to desktop
       ══════════════════════════════════════════════════════ */
    function initResizeHandler() {
        let rafId;
        window.addEventListener('resize', () => {
            cancelAnimationFrame(rafId);
            rafId = requestAnimationFrame(() => {
                if (window.innerWidth > 768) {
                    // Close mobile drawer if open
                    if (sidebar) sidebar.classList.remove('mobile-open');
                    if (overlay) overlay.style.display = 'none';
                    document.body.style.overflow = '';
                }
            });
        });
    }

    /* ══════════════════════════════════════════════════════
       BOOT
       ══════════════════════════════════════════════════════ */
    document.addEventListener('DOMContentLoaded', () => {
        initCollapse();
        initMobileDrawer();
        initProfileDropdown();
        initCollapsedTooltips();
        initActiveNav();
        initResizeHandler();

        console.log('%cSmartCM · Authenticated Shell Ready', 'color:#2563eb;font-family:serif;font-style:italic;font-size:12px');
    });

})();