/* ===== loginScript.js ===== */

// ── Password visibility toggle ───────────────────────────────────────────────
function togglePassword() {
    const input = document.getElementById('password');
    const icon  = document.getElementById('toggleIcon');

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────
function showError(id, message) {
    const el = document.getElementById(id);
    if (el) el.textContent = message;
}

function clearError(id) {
    const el = document.getElementById(id);
    if (el) el.textContent = '';
}

function markInvalid(input) {
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
}

function markValid(input) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
}

// ── Validate email format ────────────────────────────────────────────────────
function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

// ── Live validation on blur ──────────────────────────────────────────────────
const emailInput    = document.getElementById('email');
const passwordInput = document.getElementById('password');

if (emailInput) {
    emailInput.addEventListener('blur', function () {
        const val = this.value.trim();
        if (!val) {
            showError('emailError', 'Email address is required.');
            markInvalid(this);
        } else if (!isValidEmail(val)) {
            showError('emailError', 'Please enter a valid email address.');
            markInvalid(this);
        } else {
            clearError('emailError');
            markValid(this);
        }
    });

    // Clear error as user types after a failed attempt
    emailInput.addEventListener('input', function () {
        if (this.classList.contains('is-invalid')) {
            clearError('emailError');
            this.classList.remove('is-invalid');
        }
    });
}

if (passwordInput) {
    passwordInput.addEventListener('blur', function () {
        if (!this.value) {
            showError('passwordError', 'Password is required.');
            markInvalid(this);
        } else {
            clearError('passwordError');
            markValid(this);
        }
    });

    passwordInput.addEventListener('input', function () {
        if (this.classList.contains('is-invalid')) {
            clearError('passwordError');
            this.classList.remove('is-invalid');
        }
    });
}

// ── Form submit — client-side gate ───────────────────────────────────────────
const loginForm = document.getElementById('loginForm');
const submitBtn = document.getElementById('submitBtn');

if (loginForm) {
    loginForm.addEventListener('submit', function (e) {
        let isValid = true;

        // Validate email
        const emailVal = emailInput ? emailInput.value.trim() : '';
        if (!emailVal) {
            showError('emailError', 'Email address is required.');
            if (emailInput) markInvalid(emailInput);
            isValid = false;
        } else if (!isValidEmail(emailVal)) {
            showError('emailError', 'Please enter a valid email address.');
            if (emailInput) markInvalid(emailInput);
            isValid = false;
        }

        // Validate password
        if (passwordInput && !passwordInput.value) {
            showError('passwordError', 'Password is required.');
            markInvalid(passwordInput);
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
            return;
        }

        // Show loading state on successful validation
        if (submitBtn) {
            submitBtn.classList.add('loading');
            submitBtn.innerHTML = '<span class="spinner"></span> Signing in…';
        }
    });
}

// ── Auto-dismiss server-side error alert after 5 seconds ────────────────────
const errorAlert = document.getElementById('loginErrorAlert');
if (errorAlert) {
    setTimeout(function () {
        errorAlert.style.transition = 'opacity 0.4s ease';
        errorAlert.style.opacity = '0';
        setTimeout(function () {
            errorAlert.remove();
        }, 400);
    }, 5000);
}