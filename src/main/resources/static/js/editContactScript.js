/* ═══════════════════════════════════════════════════════════
   SmartCM — EDIT CONTACT PAGE SCRIPT
   ═══════════════════════════════════════════════════════════ */

(function () {
    /*
     * IIFE (Immediately Invoked Function Expression)
     * Wraps everything in a private scope so our variables
     * don't leak into the global window object and conflict
     * with other scripts (e.g. dashboardBaseScript.js)
     */
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        /*
         * DOMContentLoaded fires when HTML is fully parsed —
         * safe to query DOM elements here. We initialize each
         * feature in its own function to keep things modular.
         */
        initFileUpload();
        initSelectArrow();
        initSubmitLoader();
        initFlashDismiss();
        initFavouriteStar();
        initDescriptionCounter();
        initRemovePhoto();      // handles remove existing photo flag
    });

    /* ══════════════════════════════════════════════════════
       1. FILE UPLOAD — drag-drop zone with live preview
       ══════════════════════════════════════════════════════ */
    function initFileUpload() {
        /*
         * The actual <input type="file"> is hidden (display:none).
         * The visible upload zone is a styled div that programmatically
         * triggers a click on the hidden input when clicked.
         * This gives us full control over the UI while still
         * using the native file picker under the hood.
         */
        var fileInput   = document.getElementById('multipartFile');
        var zone        = document.getElementById('ac-upload-zone');
        var previewWrap = document.getElementById('ac-preview-wrap');
        var previewImg  = document.getElementById('ac-img-preview');
        var previewName = document.getElementById('ac-preview-name');
        var previewSize = document.getElementById('ac-preview-size');
        var uploadLabel = document.getElementById('ac-upload-label');
        var uploadSub   = document.getElementById('ac-upload-sub');
        var clearBtn    = document.getElementById('ac-clear-file');
        var fileError   = document.getElementById('fileError');
        var fileServer  = document.getElementById('fileErrorServerSide');

        if (!fileInput || !zone) return; // guard — elements must exist

        /* ── Click on zone → open file picker ── */
        zone.addEventListener('click', function (e) {
            // Don't open picker if user clicked the clear (X) button inside zone
            if (e.target === clearBtn || clearBtn.contains(e.target)) return;
            fileInput.click(); // programmatically opens native file dialog
        });

        /* ── Drag over zone → visual feedback ── */
        zone.addEventListener('dragover', function (e) {
            e.preventDefault(); // required to allow drop event to fire
            zone.classList.add('drag-over'); // CSS highlights the zone
        });

        /* ── Drag leaves zone → remove visual feedback ── */
        zone.addEventListener('dragleave', function (e) {
            // relatedTarget check prevents flickering when hovering child elements
            if (!zone.contains(e.relatedTarget)) zone.classList.remove('drag-over');
        });

        /* ── File dropped onto zone ── */
        zone.addEventListener('drop', function (e) {
            e.preventDefault();
            zone.classList.remove('drag-over');
            var file = e.dataTransfer.files[0]; // get first dropped file
            if (file) {
                validateAndApplyFile(file, fileInput, fileError, fileServer,
                    previewImg, previewName, previewSize, previewWrap, uploadLabel, uploadSub, zone);
                try {
                    /*
                     * Manually assign dropped file to the hidden input's FileList
                     * so it gets submitted with the form on POST.
                     * DataTransfer API is modern — wrapped in try/catch for older browsers.
                     */
                    var dt = new DataTransfer();
                    dt.items.add(file);
                    fileInput.files = dt.files;
                } catch (err) { /* older browsers — drop still works visually */ }
            }
        });

        /* ── User picks file via native dialog ── */
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {
                validateAndApplyFile(fileInput.files[0], fileInput, fileError, fileServer,
                    previewImg, previewName, previewSize, previewWrap, uploadLabel, uploadSub, zone);
            }
        });

        /* ── Clear button (X) inside preview — removes selected file ── */
        clearBtn.addEventListener('click', function (e) {
            e.stopPropagation(); // prevent click from bubbling up to zone (would reopen picker)
            fileInput.value           = '';   // clears the file input
            previewImg.src            = '';
            previewWrap.style.display = 'none';
            uploadLabel.textContent   = 'Click or drag image here';
            uploadSub.textContent     = 'PNG · JPG · WEBP — max 5 MB';
            zone.classList.remove('has-file'); // resets zone back to default look
            reset(fileInput, fileError, fileServer);
        });

        /*
         * validateAndApplyFile()
         * Validates file type and size, then uses FileReader API
         * to read the file as a base64 Data URL for instant preview —
         * no server round trip needed for preview.
         */
        function validateAndApplyFile(file, input, errorEl, serverEl,
            imgEl, nameEl, sizeEl, wrapEl, labelEl, subEl, zoneEl) {

            var allowed = ['image/png', 'image/jpeg', 'image/gif' , 'image/jpg'];
            var isValid = true;
            var message = '';

            // Type check — must be PNG, JPG or WEBP
            if (!allowed.includes(file.type)) {
                isValid   = false;
                message  += 'Only PNG, JPG, GIF or JPEG images allowed.<br>';
            }
            // Size check — max 5MB
            if (file.size > 2 * 1024 * 1024) {
                isValid   = false;
                message  += 'File must be under 5 MB.<br>';
            }
			if (file.size < 1024 * 10) {
			                isValid   = false;
			                message  += 'File must be above 10 KB.<br>';
			            }

            if (!isValid) {
                showError(input, errorEl, message, serverEl);
                input.value          = ''; // clear invalid file from input
                wrapEl.style.display = 'none';
                return;
            }

            showValid(input, errorEl, 'File looks good!', serverEl);

            /*
             * FileReader reads file locally in browser memory as base64 Data URL.
             * onload fires when reading is complete — then we set it as img src
             * for instant preview without uploading to server yet.
             */
            var reader    = new FileReader();
            reader.onload = function (ev) {
                imgEl.src            = ev.target.result; // base64 Data URL → shows preview
                nameEl.textContent   = file.name;
                sizeEl.textContent   = formatBytes(file.size);
                wrapEl.style.display = 'flex';           // shows preview section
                labelEl.textContent  = 'Image selected';
                subEl.textContent    = file.name;
                zoneEl.classList.add('has-file');        // green border via CSS
            };
            reader.readAsDataURL(file); // triggers onload when done
        }

        /* Converts raw bytes to human readable string e.g. 2.4 MB */
        function formatBytes(bytes) {
            if (bytes < 1024)        return bytes + ' B';
            if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
            return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
        }
    }

    /* ══════════════════════════════════════════════════════
       2. SELECT ARROW ROTATION
       ══════════════════════════════════════════════════════
       Native <select> doesn't allow styling its dropdown arrow,
       so we hide the native arrow via CSS and use a custom
       Font Awesome chevron. We rotate it 180° on open via JS.
       ══════════════════════════════════════════════════════ */
    function initSelectArrow() {
        document.querySelectorAll('.ac-select-wrap').forEach(function (wrap) {
            var select = wrap.querySelector('select');
            var arrow  = wrap.querySelector('.ac-select-arrow');
            if (!select || !arrow) return;
            select.addEventListener('mousedown', function () { arrow.classList.add('open'); });    // rotate on open
            select.addEventListener('change',    function () { arrow.classList.remove('open'); }); // reset on select
            select.addEventListener('blur',      function () { arrow.classList.remove('open'); }); // reset on close
        });
    }

    /* ══════════════════════════════════════════════════════
       3. FAVOURITE STAR ICON SWAP
       ══════════════════════════════════════════════════════
       The checkbox is hidden. Clicking the label toggles it.
       We swap the star icon between fa-regular (empty) and
       fa-solid (filled) to match the checked state.
       updateStar() is also called on load to sync with the
       pre-filled value from Thymeleaf (existing contact data).
       ══════════════════════════════════════════════════════ */
    function initFavouriteStar() {
        var favCheckbox = document.getElementById('favourite');
        var favStar     = document.getElementById('ac-fav-star');
        if (!favCheckbox || !favStar) return;

        function updateStar() {
            favStar.innerHTML = favCheckbox.checked
                ? '<i class="fa-solid fa-star"></i>'    // filled — is favourite
                : '<i class="fa-regular fa-star"></i>'; // outline — not favourite
        }
        favCheckbox.addEventListener('change', updateStar);
        updateStar(); // sync immediately on page load with pre-filled checkbox state
    }

    /* ══════════════════════════════════════════════════════
       4. DESCRIPTION CHARACTER COUNTER
       ══════════════════════════════════════════════════════
       Just sets the counter on page load since the textarea
       may already have text pre-filled by Thymeleaf.
       Live counting on input is handled in the validations
       section below (outside the IIFE).
       ══════════════════════════════════════════════════════ */
    function initDescriptionCounter() {
        var desc    = document.getElementById('description');
        var counter = document.getElementById('descriptionCharCount');
        if (!desc || !counter) return;
        // Set count immediately — Thymeleaf pre-fills textarea with existing value
        counter.textContent = desc.value.length;
    }

    /* ══════════════════════════════════════════════════════
       5. SUBMIT LOADER
       ══════════════════════════════════════════════════════
       On form submit, replaces button text with a spinner
       to give user feedback that something is happening.
       setTimeout(50ms) ensures the browser renders the
       submit first before we mutate the button — avoids
       race conditions where button mutation blocks submit.
       ══════════════════════════════════════════════════════ */
    function initSubmitLoader() {
        var form = document.getElementById('ac-form');
        var btn  = document.getElementById('ac-submit-btn');
        if (!form || !btn) return;
        form.addEventListener('submit', function () {
            setTimeout(function () {
                btn.classList.add('loading');
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i><span>Saving…</span>';
            }, 50);
        });
    }

    /* ══════════════════════════════════════════════════════
       6. AUTO-DISMISS FLASH MESSAGES
       ══════════════════════════════════════════════════════
       Flash messages (success/error) from the server are
       shown on page load. After 4 seconds they fade out
       and are removed from the DOM entirely.
       ══════════════════════════════════════════════════════ */
    function initFlashDismiss() {
        document.querySelectorAll('.ac-flash').forEach(function (el) {
            setTimeout(function () {
                el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                el.style.opacity    = '0';
                el.style.transform  = 'translateY(-8px)';
                setTimeout(function () {
                    if (el.parentNode) el.parentNode.removeChild(el); // remove from DOM after fade
                }, 520); // slightly after transition ends
            }, 4000); // wait 4 seconds before starting fade
        });
    }

    /* ══════════════════════════════════════════════════════
       7. REMOVE EXISTING PHOTO
       ══════════════════════════════════════════════════════
       Problem: browsers never pre-fill <input type="file">
       so we can't tell from an empty file input whether the
       user deliberately removed the photo or just didn't touch it.

       Solution: a hidden boolean flag `removePhoto` in ContactDto.
       When user clicks Remove → JS sets flag to "true" → server
       reads it and deletes the file + sets fileName = null in DB.

       3 cases the server handles:
         Case 1: new file uploaded  → multipartFile not empty → delete old, save new
         Case 2: remove clicked     → removePhoto = true      → delete old, set null
         Case 3: nothing touched    → both false/empty        → keep existing fileName
       ══════════════════════════════════════════════════════ */
    function initRemovePhoto() {
        var removeBtn    = document.getElementById('ec-remove-photo-btn');
        var currentStrip = document.getElementById('ec-current-photo');
        var removeFlag   = document.getElementById('removePhotoFlag');

        /*
         * Guard — these elements only exist when contact already has a photo.
         * If contact has no photo, ec-current-photo div is not rendered by Thymeleaf
         * (th:if="${contactDto.fileName != null}") so we exit safely.
         */
        if (!removeBtn || !currentStrip || !removeFlag) return;

        removeBtn.addEventListener('click', function () {
            removeFlag.value = 'true'; // ← tells server: delete this photo on save

            currentStrip.style.display = 'none'; // hide the current photo strip from UI
        });
    }

})();


/* ════════════════════════════════════════════════════════════
   DYNAMIC FIELD VALIDATIONS
   ════════════════════════════════════════════════════════════
   These run outside the IIFE because they attach directly
   to DOM elements by ID. Each validator:
     1. Debounces input (waits 400ms after user stops typing)
        to avoid firing on every keystroke
     2. Mirrors the same regex constraints as the DTO @Pattern
        annotations so user sees errors before server round trip
     3. Clears server-side errors when user starts correcting
   ════════════════════════════════════════════════════════════ */

// ── NAME ──────────────────────────────────────────────────
const nameInput      = document.getElementById('name');
const nameError      = document.getElementById('nameError');
const nameServerSide = document.getElementById('nameErrorServerSide');

let debounceTimerName;
nameInput.addEventListener('input', function () {
    const name = this.value;
    clearTimeout(debounceTimerName); // reset timer on each keystroke
    if (name === '') { reset(nameInput, nameError, nameServerSide); return; }
    debounceTimerName = setTimeout(() => {
        let isValid = true, message = '';
        if (!/^[A-Za-z]+( [A-Za-z]+)*$/.test(name)) {
            isValid = false;
            message += 'Name can contain only letters and single spaces (not trailing) between words.<br>';
        }
        if (name.length < 3)   { isValid = false; message += 'Name must be at least 3 characters long.<br>'; }
        if (name.length > 100) { isValid = false; message += 'Name must not exceed 100 characters.<br>'; }
        if (!isValid) showError(nameInput, nameError, message, nameServerSide);
        else          showValid(nameInput, nameError, 'Looks good!', nameServerSide);
    }, 400);
});

// ── NICKNAME ──────────────────────────────────────────────
const nickInput      = document.getElementById('nickName');
const nickError      = document.getElementById('nickNameError');
const nickServerSide = document.getElementById('nickNameErrorServerSide');

let debounceTimerNick;
nickInput.addEventListener('input', function () {
    const nick = this.value;
    clearTimeout(debounceTimerNick);
    if (nick === '') { reset(nickInput, nickError, nickServerSide); return; }
    debounceTimerNick = setTimeout(() => {
        let isValid = true, message = '';
        if (!/^[A-Za-z0-9]+( [A-Za-z0-9]+)*$/.test(nick)) {
            isValid = false;
            message += 'Nick name can contain only letters, digits and single spaces (not trailing) between words.<br>';
        }
        if (nick.length > 100) { isValid = false; message += 'Nick name must not exceed 100 characters.<br>'; }
        if (!isValid) showError(nickInput, nickError, message, nickServerSide);
        else          showValid(nickInput, nickError, 'Looks good!', nickServerSide);
    }, 400);
});

// ── EMAIL ─────────────────────────────────────────────────
const emailInput      = document.getElementById('email');
const emailError      = document.getElementById('emailError');
const emailServerSide = document.getElementById('emailErrorServerSide');

let debounceTimerEmail;
emailInput.addEventListener('input', function () {
    const email = this.value.trim();
    clearTimeout(debounceTimerEmail);
    if (email === '') { reset(emailInput, emailError, emailServerSide); return; }
    debounceTimerEmail = setTimeout(() => {
        let isValid = true, message = '';
        if (!/^[A-Za-z0-9._%+\-]+@gmail\.com$/i.test(email)) {
            isValid = false;
            message += 'Email must be a valid @gmail.com address.<br>';
        }
        if (email.length > 300) { isValid = false; message += 'Email must not exceed 300 characters.<br>'; }
        if (!isValid) showError(emailInput, emailError, message, emailServerSide);
        else          showValid(emailInput, emailError, 'Email looks good!', emailServerSide);
    }, 400);
});

// ── PHONE ─────────────────────────────────────────────────
const phoneInput      = document.getElementById('phone');
const phoneError      = document.getElementById('phoneError');
const phoneServerSide = document.getElementById('phoneErrorServerSide');

let debounceTimerPhone;
phoneInput.addEventListener('input', function () {
    const phone = this.value.trim();
    clearTimeout(debounceTimerPhone);
    if (phone === '') { reset(phoneInput, phoneError, phoneServerSide); return; }
    debounceTimerPhone = setTimeout(() => {
        let isValid = true, message = '';
        if (!/^[0-9]{10}$/.test(phone)) {
            isValid = false;
            message += 'Phone number must be exactly 10 digits.<br>';
        }
        if (!isValid) showError(phoneInput, phoneError, message, phoneServerSide);
        else          showValid(phoneInput, phoneError, 'Valid phone number!', phoneServerSide);
    }, 400);
});

// ── GROUP ─────────────────────────────────────────────────
/*
 * Group uses 'change' not 'input' because it's a <select> —
 * value only changes when user picks an option, not on keystrokes.
 */
const groupInput      = document.getElementById('group');
const groupError      = document.getElementById('groupError');
const groupServerSide = document.getElementById('groupErrorServerSide');

groupInput.addEventListener('change', function () {
    if (this.value === '') showError(groupInput, groupError, 'Please select a group.', groupServerSide);
    else                   showValid(groupInput, groupError, '', groupServerSide);
});

// ── DESCRIPTION ───────────────────────────────────────────
const descriptionInput      = document.getElementById('description');
const descriptionError      = document.getElementById('descriptionError');
const descriptionServerSide = document.getElementById('descriptionErrorServerSide');

let debounceTimerDescription;
descriptionInput.addEventListener('input', function () {
    const description = this.value;
    // Update counter live on every keystroke (no debounce needed for counter)
    document.getElementById('descriptionCharCount').textContent = description.length;
    clearTimeout(debounceTimerDescription);
    if (description === '') { reset(descriptionInput, descriptionError, descriptionServerSide); return; }
    debounceTimerDescription = setTimeout(() => {
        let isValid = true, message = '';
        if (description.length > 200) {
            isValid = false;
            message += 'Description must not exceed 200 characters.<br>';
        }
        if (!isValid) showError(descriptionInput, descriptionError, message, descriptionServerSide);
        else          showValid(descriptionInput, descriptionError,
                                'Looking good! (' + description.length + '/200)', descriptionServerSide);
    }, 400);
});

/* ════════════════════════════════════════════════════════════
   HELPER FUNCTIONS
   Used by all validators above to show/clear error states.
   ════════════════════════════════════════════════════════════ */

/*
 * showError() — marks input invalid, shows red message,
 * clears any old server-side error so both don't show at once
 */
function showError(input, errorEl, msg, serverSide) {
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
    errorEl.style.color = 'red';
    errorEl.innerHTML   = msg;
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}

/*
 * showValid() — marks input valid, shows green message,
 * clears server-side error
 */
function showValid(input, errorEl, msg, serverSide) {
    input.classList.add('is-valid');
    input.classList.remove('is-invalid');
    errorEl.style.color = 'green';
    errorEl.innerHTML   = msg;
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}

/*
 * reset() — clears all validation state when field is emptied,
 * returns input to neutral appearance
 */
function reset(input, errorEl, serverSide) {
    input.classList.remove('is-valid', 'is-invalid');
    errorEl.innerHTML   = '';
    errorEl.style.color = '';
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}

// ── AUTO-HIDE DUPLICATE/GLOBAL SERVER ERROR ───────────────
/*
 * If server returned a global error (e.g. duplicate phone),
 * it shows on page load. Auto-hides after 5 seconds.
 */
window.addEventListener('DOMContentLoaded', function () {
    const duplicateError = document.getElementById('duplicateErrorServerSide');
    if (duplicateError && duplicateError.innerText.trim() !== '') {
        setTimeout(() => {
            duplicateError.style.transition = 'opacity 0.5s';
            duplicateError.style.opacity    = '0';
            setTimeout(() => { duplicateError.style.display = 'none'; }, 500);
        }, 5000);
    }
});