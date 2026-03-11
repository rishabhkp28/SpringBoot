/* ═══════════════════════════════════════════════════════════
   SmartCM — CONTACTS PAGE SCRIPT
   Row hover highlight · Ripple on action buttons
   Same behaviour as userDashboardScript.js
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    const qsa = sel => document.querySelectorAll(sel);

    /* ── Row hover highlight ─────────────────────────────── */
    function initContactRowFx() {
        qsa('.dash-contact-row').forEach(row => {
            row.addEventListener('mouseenter', () => {
                row.style.paddingLeft      = '8px';
                row.style.borderLeftWidth  = '2px';
                row.style.borderLeftStyle  = 'solid';
                row.style.borderLeftColor  = 'rgba(37, 99, 235, 0.45)';
                row.style.transition       = 'padding-left 0.2s ease, border-color 0.2s ease';
            });
            row.addEventListener('mouseleave', () => {
                row.style.paddingLeft      = '0';
                row.style.borderLeftWidth  = '0';
            });
        });
    }

    /* ── Ripple on action buttons ────────────────────────── */
    function initRipple() {
        qsa('.dash-action-btn').forEach(btn => {
            btn.addEventListener('click', function (e) {
                const rect   = btn.getBoundingClientRect();
                const ripple = document.createElement('span');
                const size   = Math.max(rect.width, rect.height);

                Object.assign(ripple.style, {
                    position:      'absolute',
                    width:         size + 'px',
                    height:        size + 'px',
                    borderRadius:  '50%',
                    background:    'rgba(37, 99, 235, 0.18)',
                    transform:     'scale(0)',
                    left:          (e.clientX - rect.left - size / 2) + 'px',
                    top:           (e.clientY - rect.top  - size / 2) + 'px',
                    pointerEvents: 'none',
                    animation:     'dashRipple 0.5s ease forwards',
                });

                if (getComputedStyle(btn).position === 'static') {
                    btn.style.position = 'relative';
                }
                btn.style.overflow = 'hidden';
                btn.appendChild(ripple);
                setTimeout(() => ripple.remove(), 500);
            });
        });

        if (!document.getElementById('dash-ripple-style')) {
            const style = document.createElement('style');
            style.id = 'dash-ripple-style';
            style.textContent = `@keyframes dashRipple { to { transform: scale(2.5); opacity: 0; } }`;
            document.head.appendChild(style);
        }
    }

    /* ── Boot ────────────────────────────────────────────── */
    document.addEventListener('DOMContentLoaded', () => {
        initContactRowFx();
        initRipple();
        console.log('%cSmartCM Contacts · Page Ready', 'color:#e8a020;font-family:serif;font-style:italic;font-size:12px');
    });

})();