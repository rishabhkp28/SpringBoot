/* ═══════════════════════════════════════════════════════════
   SmartCM — DASHBOARD PAGE SCRIPT
   Progress bar · Counter animation · Stat card micro-fx
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    /* ── helpers ─────────────────────────────────────────── */
    const el  = id  => document.getElementById(id);
    const qsa = sel => document.querySelectorAll(sel);

    /* ══════════════════════════════════════════════════════
       1. PROFILE STRENGTH PROGRESS BAR
          Reads data-pct attribute set by Thymeleaf,
          animates from 0 → target on page load.
       ══════════════════════════════════════════════════════ */
    function initProgressBar() {
        const fill = el('dashProgressFill');
        const pct  = el('dashProgressPct');
        if (!fill) return;

        const target = parseInt(fill.getAttribute('data-pct') || '60', 10);

        // Slight delay so CSS transition fires after paint
        setTimeout(() => {
            fill.style.width = target + '%';
        }, 500);

        // Optional: animate the percentage number counting up
        if (pct) {
            let current = 0;
            const step  = target / 40; // 40 frames ≈ ~0.7s
            const timer = setInterval(() => {
                current = Math.min(current + step, target);
                pct.textContent = Math.round(current) + '%';
                if (current >= target) clearInterval(timer);
            }, 18);
        }
    }

    /* ══════════════════════════════════════════════════════
       2. STAT COUNTER ANIMATION
          Animates numbers from 0 up to their displayed value.
          Reads the rendered text content as the target.
       ══════════════════════════════════════════════════════ */
    function initStatCounters() {
        qsa('.dash-stat-val').forEach(el => {
            const raw    = el.textContent.trim();
            const target = parseInt(raw.replace(/[^0-9]/g, ''), 10);
            if (isNaN(target) || target === 0) return;

            // Replace with 0 to start count
            el.textContent = '0';
            let current = 0;
            const duration = 900; // ms
            const startTime = performance.now();

            function tick(now) {
                const elapsed  = now - startTime;
                const progress = Math.min(elapsed / duration, 1);
                // ease-out cubic
                const eased = 1 - Math.pow(1 - progress, 3);
                current = Math.round(eased * target);
                el.textContent = current.toLocaleString();
                if (progress < 1) requestAnimationFrame(tick);
            }

            // Small delay so card fade-in plays first
            setTimeout(() => requestAnimationFrame(tick), 300);
        });
    }

    /* ══════════════════════════════════════════════════════
       3. CONTACT ROW HOVER HIGHLIGHT
          Subtle left-border highlight on hover.
       ══════════════════════════════════════════════════════ */
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

    /* ══════════════════════════════════════════════════════
       4. QUICK ACTION BUTTON — ripple on click
       ══════════════════════════════════════════════════════ */
    function initRipple() {
        qsa('.dash-action-btn').forEach(btn => {
            btn.addEventListener('click', function (e) {
                const rect   = btn.getBoundingClientRect();
                const ripple = document.createElement('span');
                const size   = Math.max(rect.width, rect.height);

                Object.assign(ripple.style, {
                    position:     'absolute',
                    width:        size + 'px',
                    height:       size + 'px',
                    borderRadius: '50%',
                    background:   'rgba(37, 99, 235, 0.18)',
                    transform:    'scale(0)',
                    left:         (e.clientX - rect.left - size / 2) + 'px',
                    top:          (e.clientY - rect.top  - size / 2) + 'px',
                    pointerEvents:'none',
                    animation:    'dashRipple 0.5s ease forwards',
                });

                // Ensure btn has position:relative for ripple to sit inside
                if (getComputedStyle(btn).position === 'static') {
                    btn.style.position = 'relative';
                }
                btn.style.overflow = 'hidden';
                btn.appendChild(ripple);
                setTimeout(() => ripple.remove(), 500);
            });
        });

        // Inject ripple keyframe once
        if (!document.getElementById('dash-ripple-style')) {
            const style = document.createElement('style');
            style.id = 'dash-ripple-style';
            style.textContent = `
                @keyframes dashRipple {
                    to { transform: scale(2.5); opacity: 0; }
                }
            `;
            document.head.appendChild(style);
        }
    }

    /* ══════════════════════════════════════════════════════
       BOOT — run after DOM is ready
       ══════════════════════════════════════════════════════ */
    document.addEventListener('DOMContentLoaded', () => {
        initProgressBar();
        initStatCounters();
        initContactRowFx();
        initRipple();

        console.log('%cSmartCM Dashboard · Page Ready', 'color:#e8a020;font-family:serif;font-style:italic;font-size:12px');
    });

})();