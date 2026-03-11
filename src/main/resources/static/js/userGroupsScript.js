/* ═══════════════════════════════════════════════════════════
   SmartCM — GROUPS PAGE SCRIPT
   Card ripple on click · Count animate on load
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    const qsa = sel => document.querySelectorAll(sel);

    /* ══════════════════════════════════════════════════════
       1. RIPPLE ON CARD CLICK
       ══════════════════════════════════════════════════════ */
    function initRipple() {
        qsa('.grp-card').forEach(card => {
            card.addEventListener('click', function (e) {
                const rect   = card.getBoundingClientRect();
                const ripple = document.createElement('span');
                const size   = Math.max(rect.width, rect.height) * 1.6;

                Object.assign(ripple.style, {
                    position:      'absolute',
                    width:         size + 'px',
                    height:        size + 'px',
                    borderRadius:  '50%',
                    background:    'rgba(255, 255, 255, 0.06)',
                    transform:     'scale(0)',
                    left:          (e.clientX - rect.left - size / 2) + 'px',
                    top:           (e.clientY - rect.top  - size / 2) + 'px',
                    pointerEvents: 'none',
                    animation:     'grpRipple 0.55s ease forwards',
                });

                card.appendChild(ripple);
                setTimeout(() => ripple.remove(), 560);
            });
        });

        if (!document.getElementById('grp-ripple-style')) {
            const style = document.createElement('style');
            style.id = 'grp-ripple-style';
            style.textContent = `@keyframes grpRipple { to { transform: scale(1); opacity: 0; } }`;
            document.head.appendChild(style);
        }
    }

    /* ══════════════════════════════════════════════════════
       2. COUNT NUMBER ANIMATE (0 → value)
       ══════════════════════════════════════════════════════ */
    function initCounters() {
        qsa('.grp-card-count').forEach(el => {
            const raw    = el.textContent.trim();
            const target = parseInt(raw.replace(/[^0-9]/g, ''), 10);
            if (isNaN(target) || target === 0) return;

            el.textContent = '0 contacts';
            const duration  = 800;
            const startTime = performance.now();

            function tick(now) {
                const progress = Math.min((now - startTime) / duration, 1);
                const eased    = 1 - Math.pow(1 - progress, 3);
                el.textContent = Math.round(eased * target) + ' contacts';
                if (progress < 1) requestAnimationFrame(tick);
            }

            setTimeout(() => requestAnimationFrame(tick), 200);
        });
    }

    /* ── Boot ────────────────────────────────────────────── */
    document.addEventListener('DOMContentLoaded', () => {
        initRipple();
        initCounters();
        console.log('%cSmartCM Groups · Page Ready', 'color:#e8a020;font-family:serif;font-style:italic;font-size:12px');
    });

})();