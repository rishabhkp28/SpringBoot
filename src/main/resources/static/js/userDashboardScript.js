/* ═══════════════════════════════════════════════════════════
   SmartCM — DASHBOARD PAGE SCRIPT
   Progress bar · Stat counter · Contact row effects
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    /* ── shorthand ───────────────────────────────────────── */
    const el  = id  => document.getElementById(id);
    const qsa = sel => document.querySelectorAll(sel);

    /* ══════════════════════════════════════════════════════
       1. PROFILE STRENGTH PROGRESS BAR
       Reads data-pct attribute set by Thymeleaf and
       animates the fill width after a short delay.
       ══════════════════════════════════════════════════════ */
    function initProgressBar() {
        const fill = el('dashProgressFill');
        const pct  = el('dashProgressPct');
        if (!fill) return;

        const target = parseInt(fill.dataset.pct, 10) || 0;
        const clamped = Math.min(Math.max(target, 0), 100);

        // Small delay so the CSS transition fires visibly on load
        setTimeout(() => {
            fill.style.width = clamped + '%';
        }, 320);

        // Animate the percentage counter up
        if (pct) {
            let current = 0;
            const step  = Math.ceil(clamped / 40); // ~40 frames
            const timer = setInterval(() => {
                current += step;
                if (current >= clamped) {
                    current = clamped;
                    clearInterval(timer);
                }
                pct.textContent = current + '%';
            }, 28);
        }
    }

    /* ══════════════════════════════════════════════════════
       2. STAT VALUE COUNTER ANIMATION
       Counts up each stat number from 0 on page load.
       ══════════════════════════════════════════════════════ */
    function initStatCounters() {
        qsa('.dash-stat-val').forEach(valEl => {
            const raw = valEl.textContent.trim();
            const num = parseInt(raw, 10);
            if (isNaN(num) || raw === '—') return; // skip non-numeric / placeholder

            valEl.textContent = '0';
            let current  = 0;
            const frames = 40;
            const step   = Math.max(1, Math.ceil(num / frames));

            const timer = setInterval(() => {
                current += step;
                if (current >= num) {
                    current = num;
                    clearInterval(timer);
                }
                valEl.textContent = current.toLocaleString();
            }, 28);
        });
    }

    /* ══════════════════════════════════════════════════════
       3. CONTACT ROW — staggered entrance
       Adds a subtle stagger to each contact row so they
       animate in one by one rather than all at once.
       ══════════════════════════════════════════════════════ */
    function initContactRowStagger() {
        qsa('.dash-contact-row').forEach((row, i) => {
            row.style.opacity    = '0';
            row.style.transform  = 'translateX(-8px)';
            row.style.transition = 'opacity 0.30s ease, transform 0.30s ease';

            setTimeout(() => {
                row.style.opacity   = '1';
                row.style.transform = 'translateX(0)';
            }, 420 + i * 65); // starts after panel fade-in
        });
    }

    /* ══════════════════════════════════════════════════════
       4. ACTION BUTTONS — ripple on click
       Lightweight CSS-driven ripple effect.
       ══════════════════════════════════════════════════════ */
    function initActionRipple() {
        qsa('.dash-action-btn').forEach(btn => {
            btn.addEventListener('click', function (e) {
                // Don't prevent navigation — purely cosmetic
                const ripple = document.createElement('span');

                Object.assign(ripple.style, {
                    position:     'absolute',
                    borderRadius: '50%',
                    background:   'rgba(147, 180, 255, 0.18)',
                    width:        '120px',
                    height:       '120px',
                    pointerEvents:'none',
                    transform:    'scale(0)',
                    transition:   'transform 0.45s ease, opacity 0.45s ease',
                    opacity:      '1',
                });

                // Ensure parent is positioned
                if (getComputedStyle(btn).position === 'static') {
                    btn.style.position = 'relative';
                }
                btn.style.overflow = 'hidden';

                const rect = btn.getBoundingClientRect();
                ripple.style.left = (e.clientX - rect.left - 60) + 'px';
                ripple.style.top  = (e.clientY - rect.top  - 60) + 'px';

                btn.appendChild(ripple);
                requestAnimationFrame(() => {
                    ripple.style.transform = 'scale(2)';
                    ripple.style.opacity   = '0';
                });

                setTimeout(() => ripple.remove(), 480);
            });
        });
    }

    /* ══════════════════════════════════════════════════════
       5. STAT CARDS — hover glow tint per color
       Adds a faint colored ambient glow matching the card
       icon color when the card is hovered.
       ══════════════════════════════════════════════════════ */
    function initStatCardGlow() {
        const glowMap = {
            'dash-si-blue':   'rgba(37, 99, 235, 0.08)',
            'dash-si-gold':   'rgba(232, 160, 32, 0.07)',
            'dash-si-green':  'rgba(34, 197, 94, 0.07)',
            'dash-si-purple': 'rgba(139, 92, 246, 0.08)',
        };

        qsa('.dash-stat-card').forEach(card => {
            const iconWrap = card.querySelector('.dash-stat-icon-wrap');
            if (!iconWrap) return;

            let glowColor = 'transparent';
            for (const [cls, color] of Object.entries(glowMap)) {
                if (iconWrap.classList.contains(cls)) { glowColor = color; break; }
            }

            card.addEventListener('mouseenter', () => {
                card.style.background = glowColor === 'transparent'
                    ? ''
                    : `rgba(13, 16, 24, 0.82)`;
                card.style.boxShadow = `0 14px 44px rgba(0,0,0,0.38), 0 0 0 1px ${glowColor}`;
            });
            card.addEventListener('mouseleave', () => {
                card.style.background = '';
                card.style.boxShadow  = '';
            });
        });
    }

    /* ══════════════════════════════════════════════════════
       BOOT
       ══════════════════════════════════════════════════════ */
    document.addEventListener('DOMContentLoaded', () => {
        initProgressBar();
        initStatCounters();
        initContactRowStagger();
        initActionRipple();
        initStatCardGlow();

        console.log('%cSmartCM · Dashboard Page Ready', 'color:#4ade80;font-family:serif;font-style:italic;font-size:12px');
    });

})();