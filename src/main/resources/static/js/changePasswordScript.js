/* ═══════════════════════════════════════════════════════════
   SmartCM — CHANGE PASSWORD PAGE SCRIPT
   ═══════════════════════════════════════════════════════════
   PasswordDto fields:
     currentPassword    — no DTO constraint, BCrypt in service
     newPassword        — @NotBlank @Size(max=20) @Pattern(strong)
     confirmNewPassword — no constraint, match checked in controller

   KEY BEHAVIOURS:
   • New + Confirm fields are PHYSICALLY LOCKED (pointer-events:none,
     opacity dim, not focusable) until currentPassword has a value.
   • If user bypasses via DevTools and submits anyway, the guard
     catches it, clears both fields, re-locks them, and errors.
   • All server-side errors auto-dismiss after 7 s.
   • Server-side error for a field also clears the moment the
     user starts typing in that field.
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    /* ═══════════════════════════════════════════════════════
       PASSWORD POLICY — single source of truth
       ═══════════════════════════════════════════════════════ */
    var PW_POLICY = {
        regex  : /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/,
        checks : {
            lenMin  : function (v) { return v.length >= 8; },
            lenMax  : function (v) { return v.length <= 20; },
            lower   : function (v) { return /[a-z]/.test(v); },
            upper   : function (v) { return /[A-Z]/.test(v); },
            digit   : function (v) { return /\d/.test(v); },
            special : function (v) { return /[@$!%*?&]/.test(v); },
        }
    };

    /* Centralised validator — returns { valid, errors[] } */
    function validatePassword(val) {
        var errors = [];
        if (!PW_POLICY.checks.lenMin(val))  errors.push('At least 8 characters.');
        if (!PW_POLICY.checks.lenMax(val))  errors.push('Max 20 characters.');
        if (!PW_POLICY.checks.lower(val))   errors.push('Add a lowercase letter.');
        if (!PW_POLICY.checks.upper(val))   errors.push('Add an uppercase letter.');
        if (!PW_POLICY.checks.digit(val))   errors.push('Add a digit (0–9).');
        if (!PW_POLICY.checks.special(val)) errors.push('Add a special character (@$!%*?&).');
        return { valid: errors.length === 0, errors: errors };
    }

    /* Hint banner debounce */
    var hintTimer;
    function safeUpdateHint(state) {
        clearTimeout(hintTimer);
        hintTimer = setTimeout(function () { updateHintBanner(state); }, 100);
    }

    /* ──────────────────────────────────────────────────────
       LOCK / UNLOCK HELPERS
       lock()   → dims field group, disables input + button
       unlock() → restores full interactivity
    ────────────────────────────────────────────────────── */
    function lockGroup(groupId) {
        var group = document.getElementById(groupId);
        if (!group) return;
        group.classList.add('cp-field-locked');
        var inputs = group.querySelectorAll('input, button');
        inputs.forEach(function (el) {
            el.setAttribute('tabindex', '-1');
            el.setAttribute('aria-disabled', 'true');
        });
    }

    function unlockGroup(groupId) {
        var group = document.getElementById(groupId);
        if (!group) return;
        group.classList.remove('cp-field-locked');
        var inputs = group.querySelectorAll('input, button');
        inputs.forEach(function (el) {
            el.removeAttribute('tabindex');
            el.removeAttribute('aria-disabled');
        });
    }

    /* Clear a locked field's value + validation state */
    function clearLockedField(inputId, errorId, serverSideId) {
        var input = document.getElementById(inputId);
        if (input) input.value = '';
        resetField(
            document.getElementById(inputId),
            document.getElementById(errorId),
            document.getElementById(serverSideId)
        );
        var reqWrap = document.getElementById('cp-req-wrap');
        if (reqWrap && inputId === 'cp-new-password') {
            reqWrap.style.display = 'none';
            resetChecklist();
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        initToggles();
        initLocking();          /* sets up the lock/unlock logic */
        initNewPassword();
        initConfirmPassword();
        initSubmitGuard();
        initSubmitLoader();
        initFlashDismiss();
        initServerSideDismiss();
        safeUpdateHint('idle');
    });

    /* ══════════════════════════════════════════════════════
       1. SHOW / HIDE TOGGLES
       ══════════════════════════════════════════════════════ */
    function initToggles() {
        [
            ['cp-current-password', 'cp-toggle-current', 'cp-eye-current'],
            ['cp-new-password',     'cp-toggle-new',     'cp-eye-new'],
            ['cp-confirm-password', 'cp-toggle-confirm', 'cp-eye-confirm'],
        ].forEach(function (trio) {
            var input = document.getElementById(trio[0]);
            var btn   = document.getElementById(trio[1]);
            var icon  = document.getElementById(trio[2]);
            if (!input || !btn || !icon) return;
            btn.addEventListener('click', function () {
                if (input.type === 'password') {
                    input.type = 'text'; icon.className = 'fa-regular fa-eye-slash';
                } else {
                    input.type = 'password'; icon.className = 'fa-regular fa-eye';
                }
            });
        });
    }

    /* ══════════════════════════════════════════════════════
       2. LOCKING — currentPassword gates new + confirm
       ═══════════════════════════════════════════════════════
       On load: both new and confirm are locked.
       User types in current → non-empty → unlock new + confirm.
       User clears current   → re-lock + wipe new + confirm.
       ══════════════════════════════════════════════════════ */
    function initLocking() {
        var curInput  = document.getElementById('cp-current-password');
        var curError  = document.getElementById('currentPasswordError');
        var curServer = document.getElementById('currentPasswordServerSide');

        if (!curInput) return;

        /* Lock both groups immediately on load */
        lockGroup('cp-fg-new');
        lockGroup('cp-fg-confirm');

        curInput.addEventListener('input', function () {
            var val = this.value;

            /* Clear current-password error as user types */
            if (val.trim() !== '') {
                resetField(curInput, curError, curServer);
            }

            if (val.trim() !== '') {
                /* Non-empty → unlock new + confirm */
                unlockGroup('cp-fg-new');
                unlockGroup('cp-fg-confirm');
            } else {
                /*
                 * Cleared → re-lock and wipe new + confirm.
                 * This prevents stale valid/invalid states.
                 */
                lockGroup('cp-fg-new');
                lockGroup('cp-fg-confirm');
                clearLockedField('cp-new-password',     'newPasswordError',     'newPasswordServerSide');
                clearLockedField('cp-confirm-password', 'confirmPasswordError', 'confirmPasswordServerSide');
            }

            safeUpdateHint(computeHintState());
        });
    }

    /* ══════════════════════════════════════════════════════
       3. NEW PASSWORD — checklist + centralised validator
       ══════════════════════════════════════════════════════ */
    function initNewPassword() {
        var input    = document.getElementById('cp-new-password');
        var errorEl  = document.getElementById('newPasswordError');
        var serverEl = document.getElementById('newPasswordServerSide');
        var reqWrap  = document.getElementById('cp-req-wrap');
        var debounce;

        if (!input) return;

        input.addEventListener('input', function () {
            var val = this.value;
            clearTimeout(debounce);

            /* Checklist — instant update */
            if (val.length > 0) {
                reqWrap.style.display = 'block';
                updateChecklist(val);
            } else {
                reqWrap.style.display = 'none';
                resetChecklist();
            }

            if (val === '') {
                resetField(input, errorEl, serverEl);
                safeUpdateHint(computeHintState());
                return;
            }

            debounce = setTimeout(function () {
                var result = validatePassword(val);
                if (result.valid) {
                    showValid(input, errorEl, 'Strong password!', serverEl);
                } else {
                    showError(input, errorEl, result.errors[0], serverEl);
                }
                var cfmEl = document.getElementById('cp-confirm-password');
                if (cfmEl && cfmEl.value !== '') checkMatch();
                safeUpdateHint(computeHintState());
            }, 400);
        });
    }

    /* Checklist helpers */
    function updateChecklist(val) {
        setReq('req-len-min', PW_POLICY.checks.lenMin(val));
        setReq('req-len-max', PW_POLICY.checks.lenMax(val));
        setReq('req-lower',   PW_POLICY.checks.lower(val));
        setReq('req-upper',   PW_POLICY.checks.upper(val));
        setReq('req-digit',   PW_POLICY.checks.digit(val));
        setReq('req-special', PW_POLICY.checks.special(val));
    }

    function setReq(id, met) {
        var li   = document.getElementById(id);
        var icon = li ? li.querySelector('.req-icon') : null;
        if (!li) return;
        li.classList.toggle('req-met',   met);
        li.classList.toggle('req-unmet', !met);
        if (icon) {
            icon.className = met
                ? 'req-icon fa-solid fa-circle-check'
                : 'req-icon fa-solid fa-circle-xmark';
        }
    }

    function resetChecklist() {
        ['req-len-min','req-len-max','req-lower','req-upper','req-digit','req-special']
        .forEach(function (id) {
            var li = document.getElementById(id);
            var ic = li ? li.querySelector('.req-icon') : null;
            if (!li) return;
            li.classList.remove('req-met', 'req-unmet');
            if (ic) ic.className = 'req-icon fa-solid fa-circle-xmark';
        });
    }

    /* ══════════════════════════════════════════════════════
       4. CONFIRM PASSWORD — match only
       ══════════════════════════════════════════════════════ */
    function initConfirmPassword() {
        var input = document.getElementById('cp-confirm-password');
        if (!input) return;
        input.addEventListener('input', function () {
            checkMatch();
            safeUpdateHint(computeHintState());
        });
    }

    function checkMatch() {
        var nwEl      = document.getElementById('cp-new-password');
        var cfmEl     = document.getElementById('cp-confirm-password');
        var cfmError  = document.getElementById('confirmPasswordError');
        var cfmServer = document.getElementById('confirmPasswordServerSide');
        if (!nwEl || !cfmEl) return;

        var nw  = nwEl.value;
        var cfm = cfmEl.value;

        if (cfm === '') { resetField(cfmEl, cfmError, cfmServer); return; }

        if (cfm !== nw) {
            showError(cfmEl, cfmError, 'Passwords do not match.', cfmServer);
        } else {
            var result = validatePassword(nw);
            if (result.valid) {
                showValid(cfmEl, cfmError, 'Passwords match ✓', cfmServer);
            } else {
                resetField(cfmEl, cfmError, cfmServer);
            }
        }
    }

    /* ══════════════════════════════════════════════════════
       5. HINT BANNER (debounced 100ms)
       ══════════════════════════════════════════════════════ */
    function computeHintState() {
        var curEl = document.getElementById('cp-current-password');
        var nwEl  = document.getElementById('cp-new-password');
        var cfmEl = document.getElementById('cp-confirm-password');

        var cur = curEl ? curEl.value.trim() : '';
        var nw  = nwEl  ? nwEl.value         : '';
        var cfm = cfmEl ? cfmEl.value         : '';

        if (cur === '' && nw === '' && cfm === '') return 'idle';

        var nwResult = validatePassword(nw);

        if (cur === '' && (nw !== '' || cfm !== '')) return 'need-current';
        if (cur !== '' && nw === '' && cfm === '')   return 'need-new';
        if (cur !== '' && nw !== '' && cfm === '')   return 'need-confirm';
        if (cur !== '' && nw !== '' && cfm !== '' && nw !== cfm) return 'mismatch';
        if (cur !== '' && nwResult.valid && nw === cfm)          return 'ready';
        if (cur !== '' && !nwResult.valid)                       return 'pw-weak';
        return 'idle';
    }

    function updateHintBanner(state) {
        var banner   = document.getElementById('cp-hint-banner');
        var hintText = document.getElementById('cp-hint-text');
        var hintIcon = banner ? banner.querySelector('.cp-hint-icon') : null;
        if (!banner || !hintText) return;

        banner.classList.remove('cp-hint-warn', 'cp-hint-error');
        banner.style.background  = '';
        banner.style.borderColor = '';
        hintText.style.color     = '';
        if (hintIcon) { hintIcon.className = 'fa-solid fa-circle-info cp-hint-icon'; hintIcon.style.color = ''; }

        var messages = {
            'idle'        : 'Leave all fields empty to cancel without changes.',
            'need-current': 'Enter your current password to unlock the other fields.',
            'need-new'    : 'Enter and confirm your new password to proceed.',
            'need-confirm': 'Re-enter your new password to confirm.',
            'mismatch'    : 'New password and confirmation do not match.',
            'pw-weak'     : 'New password does not meet the requirements above.',
            'ready'       : 'All good — click Update Password to save.',
        };

        hintText.textContent = messages[state] || messages['idle'];

        if (state === 'need-current' || state === 'mismatch' || state === 'pw-weak') {
            banner.classList.add('cp-hint-warn');
            if (hintIcon) hintIcon.className = 'fa-solid fa-triangle-exclamation cp-hint-icon';
        } else if (state === 'ready') {
            banner.style.background  = 'rgba(52,211,153,0.08)';
            banner.style.borderColor = 'rgba(52,211,153,0.28)';
            hintText.style.color     = '#34d399';
            if (hintIcon) { hintIcon.className = 'fa-solid fa-circle-check cp-hint-icon'; hintIcon.style.color = '#34d399'; }
        }
    }

    /* ══════════════════════════════════════════════════════
       6. SUBMIT GUARD
       ═══════════════════════════════════════════════════════
       Case A — all empty           → noOp flag, let through
       Case B — current empty but   → block, error on current,
                new/confirm filled    re-lock + clear new/confirm
       Case B2 — current filled,    → block, error on new
                 new missing
       Case B3 — current + new      → block, error on confirm
                 filled, confirm empty
       Case C — new fails validation → block, error on new
       Case D — mismatch            → block, error on confirm
       Case E — all valid + match   → let through
       ══════════════════════════════════════════════════════ */
    function initSubmitGuard() {
        var form     = document.getElementById('cp-form');
        var noopFlag = document.getElementById('cp-noop-flag');
        if (!form) return;

        form.addEventListener('submit', function (e) {
            var curEl  = document.getElementById('cp-current-password');
            var nwEl   = document.getElementById('cp-new-password');
            var cfmEl  = document.getElementById('cp-confirm-password');
            var curErr = document.getElementById('currentPasswordError');
            var nwErr  = document.getElementById('newPasswordError');
            var cfmErr = document.getElementById('confirmPasswordError');

            var cur = curEl ? curEl.value.trim() : '';
            var nw  = nwEl  ? nwEl.value         : '';
            var cfm = cfmEl ? cfmEl.value         : '';

            /* Case A — all empty → noOp */
            if (cur === '' && nw === '' && cfm === '') {
                if (noopFlag) noopFlag.value = 'true';
                return;
            }

            /*
             * Case B — bypass attempt: new/confirm filled but current empty.
             * Re-lock and clear both fields so the bypass is undone.
             */
            if (cur === '' && (nw !== '' || cfm !== '')) {
                e.preventDefault();
                /* Wipe + re-lock the bypassed fields */
                lockGroup('cp-fg-new');
                lockGroup('cp-fg-confirm');
                clearLockedField('cp-new-password',     'newPasswordError',     'newPasswordServerSide');
                clearLockedField('cp-confirm-password', 'confirmPasswordError', 'confirmPasswordServerSide');
                showError(curEl, curErr, 'Enter your current password first to make changes.', null);
                shake(curEl);
                curEl.focus();
                safeUpdateHint('need-current');
                return;
            }

            /* Case B2 — current filled, new missing */
            if (cur !== '' && nw === '') {
                e.preventDefault();
                showError(nwEl, nwErr, 'Please enter your new password.', null);
                shake(nwEl);
                nwEl.focus();
                safeUpdateHint('need-new');
                return;
            }

            /* Case B3 — current + new filled, confirm missing */
            if (cur !== '' && nw !== '' && cfm === '') {
                e.preventDefault();
                showError(cfmEl, cfmErr, 'Please confirm your new password.', null);
                shake(cfmEl);
                cfmEl.focus();
                safeUpdateHint('need-confirm');
                return;
            }

            /* Case C — new password fails validation */
            var nwResult = validatePassword(nw);
            if (!nwResult.valid) {
                e.preventDefault();
                showError(nwEl, nwErr, nwResult.errors[0], null);
                shake(nwEl);
                scrollToFirstInvalid(form);
                safeUpdateHint('pw-weak');
                return;
            }

            /* Case D — mismatch */
            if (nw !== cfm) {
                e.preventDefault();
                showError(cfmEl, cfmErr, 'Passwords do not match.', null);
                shake(cfmEl);
                cfmEl.focus();
                safeUpdateHint('mismatch');
                return;
            }

            /* Case E — all valid, let through */
        });
    }

    function scrollToFirstInvalid(form) {
        var el = form.querySelector('.is-invalid')
              || form.querySelector('.cp-validation-msg:not(:empty)');
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function shake(el) {
        if (!el) return;
        el.classList.remove('cp-shake');
        void el.offsetWidth; /* force reflow to restart animation */
        el.classList.add('cp-shake');
        el.addEventListener('animationend', function () {
            el.classList.remove('cp-shake');
        }, { once: true });
    }

    /* ══════════════════════════════════════════════════════
       7. SUBMIT LOADER
       Only spins when all checks pass (Case E).
       ══════════════════════════════════════════════════════ */
    function initSubmitLoader() {
        var form = document.getElementById('cp-form');
        var btn  = document.getElementById('cp-submit-btn');
        if (!form || !btn) return;

        form.addEventListener('submit', function () {
            setTimeout(function () {
                var cur = document.getElementById('cp-current-password');
                var nw  = document.getElementById('cp-new-password');
                var cfm = document.getElementById('cp-confirm-password');
                var curFilled = cur && cur.value.trim() !== '';
                var nwResult  = validatePassword(nw ? nw.value : '');
                var cfmMatch  = cfm && nw && cfm.value === nw.value;
                if (curFilled && nwResult.valid && cfmMatch) {
                    btn.classList.add('loading');
                    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i><span>Updating…</span>';
                }
            }, 60);
        });
    }

    /* ══════════════════════════════════════════════════════
       8. FLASH DISMISS (4 s)
       ══════════════════════════════════════════════════════ */
    function initFlashDismiss() {
        document.querySelectorAll('.cp-flash').forEach(function (el) {
            setTimeout(function () {
                el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                el.style.opacity = '0'; el.style.transform = 'translateY(-8px)';
                setTimeout(function () { if (el.parentNode) el.parentNode.removeChild(el); }, 520);
            }, 4000);
        });
    }

    /* ══════════════════════════════════════════════════════
       9. SERVER-SIDE ERROR DISMISS — 7 seconds + clear on input
       ═══════════════════════════════════════════════════════
       Each server-side section fades after 7 s.
       Additionally, when the user starts typing in the field
       that owns the error, the section clears immediately —
       no need to wait the full 7 s.
       ══════════════════════════════════════════════════════ */
    function initServerSideDismiss() {
        /*
         * Map: inputId → serverSideId
         * When user types in that input, instantly clear its
         * server-side error section.
         */
        var inputToServerSection = {
            'cp-current-password' : 'currentPasswordServerSide',
            'cp-new-password'     : 'newPasswordServerSide',
            'cp-confirm-password' : 'confirmPasswordServerSide',
        };

        /* Instant clear on input */
        Object.keys(inputToServerSection).forEach(function (inputId) {
            var sectionId = inputToServerSection[inputId];
            var input   = document.getElementById(inputId);
            var section = document.getElementById(sectionId);
            if (!input || !section) return;

            input.addEventListener('input', function () {
                if (section.textContent.trim()) {
                    section.style.transition = 'opacity 0.3s';
                    section.style.opacity    = '0';
                    setTimeout(function () {
                        section.innerHTML    = '';
                        section.style.opacity = '';
                        section.style.display = 'none';
                    }, 300);
                }
            }, { once: true }); /* only needs to fire once per page load */
        });

        /* 7-second auto-dismiss for ALL server-side sections */
        [
            'currentPasswordServerSide',
            'newPasswordServerSide',
            'confirmPasswordServerSide',
            'cp-global-error',
        ].forEach(function (id) {
            var el = document.getElementById(id);
            if (!el || !el.textContent.trim()) return;
            setTimeout(function () {
                el.style.transition = 'opacity 0.5s';
                el.style.opacity    = '0';
                setTimeout(function () {
                    el.innerHTML      = '';
                    el.style.opacity  = '';
                    el.style.display  = 'none';
                }, 500);
            }, 7000);
        });
    }

})();

/* ════════════════════════════════════════════════════════════
   HELPERS — outside IIFE, used by all validators
   ════════════════════════════════════════════════════════════ */
function showError(input, errorEl, msg, serverSide) {
    if (input)      { input.classList.add('is-invalid'); input.classList.remove('is-valid'); }
    if (errorEl)    { errorEl.style.color = '#f87171'; errorEl.innerHTML = msg; }
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}

function showValid(input, errorEl, msg, serverSide) {
    if (input)      { input.classList.add('is-valid'); input.classList.remove('is-invalid'); }
    if (errorEl)    { errorEl.style.color = '#34d399'; errorEl.innerHTML = msg; }
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}

function resetField(input, errorEl, serverSide) {
    if (input)      { input.classList.remove('is-valid', 'is-invalid'); }
    if (errorEl)    { errorEl.innerHTML = ''; errorEl.style.color = ''; }
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}