/* ===== loginScript.js ===== */

// ── Password toggle ─────────────────────────────────────
function togglePassword() {
    const input = document.getElementById('password');
    const icon  = document.getElementById('toggleIcon');

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// ── Helpers ─────────────────────────────────────────────
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

// ── Email validation ────────────────────────────────────
function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

// ── Inputs ──────────────────────────────────────────────
const emailInput    = document.getElementById('email');
const passwordInput = document.getElementById('password');

// ── Live validation ─────────────────────────────────────
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

    emailInput.addEventListener('input', function () {
        clearError('emailError');
        this.classList.remove('is-invalid');
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
        clearError('passwordError');
        this.classList.remove('is-invalid');
    });
}

// ── Form submit ─────────────────────────────────────────
const loginForm = document.getElementById('loginForm');
const submitBtn = document.getElementById('submitBtn');

if (loginForm) {
    loginForm.addEventListener('submit', function (e) {
        let isValid = true;

        const emailVal = emailInput.value.trim();

        if (!emailVal) {
            showError('emailError', 'Email address is required.');
            markInvalid(emailInput);
            isValid = false;
        } else if (!isValidEmail(emailVal)) {
            showError('emailError', 'Invalid email format.');
            markInvalid(emailInput);
            isValid = false;
        }

        if (!passwordInput.value) {
            showError('passwordError', 'Password is required.');
            markInvalid(passwordInput);
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
            return;
        }

        // Loading state
        submitBtn.classList.add('loading');
        submitBtn.innerHTML = '<span class="spinner"></span> Signing in…';
    });
}

// ── Auto-hide server error ──────────────────────────────
const errorAlert = document.getElementById('loginErrorAlert');

if (errorAlert) {
    setTimeout(() => {
        errorAlert.style.transition = 'opacity 0.4s';
        errorAlert.style.opacity = '0';

        setTimeout(() => errorAlert.remove(), 400);
    }, 4000);
}