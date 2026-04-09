/* ═══════════════════════════════════════════════════════════
   SmartCM — USER PROFILE PAGE SCRIPT
   ═══════════════════════════════════════════════════════════
   UserProfileDto fields:
     name, email, bio        → validated against DTO constraints
     currentPassword         → required only when something changed
     multipartFile           → file type + size
     removePhoto / image     → change detection
     userId                  → hidden, not validated

   CHANGE DETECTION LOGIC:
     On page load, read the original values stored as
     data-original-* attributes on #up-details-card.
     On every input event (name, email, bio, file, removePhoto),
     compare current values against originals.
     If ANYTHING differs → hasChanges() returns true
       → confirm card becomes active, password becomes required.
     If NOTHING differs → hasChanges() returns false
       → confirm card stays idle, submit passes through freely.

   PHOTO STATE MACHINE:
     There are two separate preview zones:
       A) Avatar circle  (#up-avatar)        — shows current / new photo
       B) Upload strip   (#up-file-preview)  — shows newly chosen file info

     States:
       'original'  — showing the server-rendered photo (or initials if none)
       'new'       — user picked a file; avatar circle shows new preview,
                     upload strip shows filename/size
       'removed'   — user clicked the avatar ×; initials shown, removePhoto=true

     The avatar × badge (#up-avatar-remove):
       • In state 'original' → transitions to 'removed'
       • In state 'new'      → cancels the new upload, reverts to 'original'
                               (does NOT set removePhoto=true)

     The upload strip × button (#up-preview-clear):
       • Always cancels the new file, reverts avatar to 'original'
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    /* ── Read originals from the card's data attributes ─── */
    var detailsCard = document.getElementById('up-details-card');

    var ORIGINAL = {
        name  : detailsCard ? (detailsCard.dataset.originalName  || '') : '',
        email : detailsCard ? (detailsCard.dataset.originalEmail || '') : '',
        bio   : detailsCard ? (detailsCard.dataset.originalBio   || '') : '',
        image : detailsCard ? (detailsCard.dataset.originalImage || '') : '',
    };

    /*
     * photoChanged tracks whether the user uploaded a new file
     * or clicked "Remove photo". It's separate from the text fields
     * because a file input can't be pre-filled for comparison.
     */
    var photoChanged = false;

    /*
     * photoState tracks which of the three avatar states we are in:
     *   'original' — server-rendered photo (or initials if no photo)
     *   'new'      — user just uploaded a new file
     *   'removed'  — user clicked the avatar × badge on an existing photo
     */
    var photoState = 'original';

    /*
     * originalAvatarSrc stores the src of the server-rendered <img>
     * so we can restore it when the user cancels a new upload.
     * Captured once on DOMContentLoaded.
     */
    var originalAvatarSrc = '';

    document.addEventListener('DOMContentLoaded', function () {
        /* Capture the original avatar src before anything changes */
        var avatarImg = document.getElementById('up-avatar-img');
        if (avatarImg) originalAvatarSrc = avatarImg.src;

        initFileUpload();
        initRemovePhoto();
        initPasswordToggle();
        initBioCounter();
        initChangeDetection();
        initSubmitGuard();
        initSubmitLoader();
        initFlashDismiss();
        initServerSideDismiss();
    });

    /* ══════════════════════════════════════════════════════
       CHANGE DETECTION HELPER
       Returns true if any field differs from its original value
       OR if the user changed the photo.
       ══════════════════════════════════════════════════════ */
    function hasChanges() {
        var nameEl  = document.getElementById('up-name');
        var emailEl = document.getElementById('up-email');
        var bioEl   = document.getElementById('up-bio');

        var currentName  = nameEl  ? nameEl.value.trim()  : '';
        var currentEmail = emailEl ? emailEl.value.trim() : '';
        var currentBio   = bioEl   ? bioEl.value.trim()   : '';

        if (currentName  !== ORIGINAL.name.trim())  return true;
        if (currentEmail !== ORIGINAL.email.trim())  return true;
        if (currentBio   !== ORIGINAL.bio.trim())    return true;
        if (photoChanged)                             return true;
        return false;
    }

    /* ══════════════════════════════════════════════════════
       UPDATE CONFIRM CARD UI
       ══════════════════════════════════════════════════════ */
    function updateConfirmCard() {
        var card    = document.getElementById('up-confirm-card');
        var sub     = document.getElementById('up-confirm-sub');
        var star    = document.getElementById('up-pw-required-star');
        var pwInput = document.getElementById('up-current-password');

        if (!card) return;

        if (hasChanges()) {
            card.classList.add('up-confirm-active');
            if (sub)  sub.textContent  = 'Enter your current password to save changes';
            if (star) star.style.display = 'inline';
            if (pwInput) pwInput.placeholder = 'Required to save changes';
        } else {
            card.classList.remove('up-confirm-active');
            if (sub)  sub.textContent  = 'No changes detected — password not required';
            if (star) star.style.display = 'none';
            if (pwInput) pwInput.placeholder = 'Only needed if you changed something';
        }
    }

    /* ══════════════════════════════════════════════════════
       RESTORE AVATAR TO ORIGINAL
       Called when user cancels a new upload OR undoes remove.
       Puts the avatar circle back to exactly what the server
       rendered, without touching removePhoto flag.
       ══════════════════════════════════════════════════════ */
    function restoreAvatarToOriginal() {
        var avatarImg  = document.getElementById('up-avatar-img');
        var avatarInit = document.getElementById('up-avatar-initials');
        var removeBtn  = document.getElementById('up-avatar-remove');

        if (ORIGINAL.image !== '') {
            /*
             * Original had a photo.
             * Show the img element with the original src.
             * The img element may have been hidden or its src replaced.
             */
            if (avatarImg) {
                avatarImg.src           = originalAvatarSrc;
                avatarImg.style.display = 'block';
            } else {
                /*
                 * img element was removed from DOM (shouldn't happen in
                 * normal flow but guard anyway) — recreate it.
                 */
                var newImg      = document.createElement('img');
                newImg.src      = originalAvatarSrc;
                newImg.id       = 'up-avatar-img';
                newImg.className = 'up-avatar-img';
                newImg.alt      = 'Profile photo';
                var avatarDiv   = document.getElementById('up-avatar');
                if (avatarDiv) avatarDiv.insertBefore(newImg, avatarDiv.firstChild);
            }
            /* Hide initials if visible */
            if (avatarInit) avatarInit.style.display = 'none';
            /* Show the remove badge again so user can still remove */
            if (removeBtn) removeBtn.style.display = 'flex';
        } else {
            /*
             * Original had NO photo — just show initials.
             * No remove badge needed.
             */
            if (avatarImg) avatarImg.style.display = 'none';
            if (avatarInit) avatarInit.style.display = 'flex';
            if (removeBtn) removeBtn.style.display = 'none';
        }
    }

    /* ══════════════════════════════════════════════════════
       1. FILE UPLOAD
       ══════════════════════════════════════════════════════ */
    function initFileUpload() {
        var fileInput   = document.getElementById('up-file-input');
        var zone        = document.getElementById('up-upload-zone');
        var previewWrap = document.getElementById('up-file-preview');
        var previewImg  = document.getElementById('up-preview-thumb');
        var previewName = document.getElementById('up-preview-name');
        var previewSize = document.getElementById('up-preview-size');
        var uploadLabel = document.getElementById('up-upload-label');
        var uploadSub   = document.getElementById('up-upload-sub');
        var clearBtn    = document.getElementById('up-preview-clear');
        var fileError   = document.getElementById('multipartFileError');
        var fileServer  = document.getElementById('multipartFileErrorServerSide');
        var removeFlag  = document.getElementById('up-remove-photo-flag');

        if (!fileInput || !zone) return;

        zone.addEventListener('click', function (e) {
            if (clearBtn && (e.target === clearBtn || clearBtn.contains(e.target))) return;
            fileInput.click();
        });
        zone.addEventListener('dragover', function (e) { e.preventDefault(); zone.classList.add('drag-over'); });
        zone.addEventListener('dragleave', function (e) { if (!zone.contains(e.relatedTarget)) zone.classList.remove('drag-over'); });
        zone.addEventListener('drop', function (e) {
            e.preventDefault(); zone.classList.remove('drag-over');
            var file = e.dataTransfer.files[0];
            if (file) {
                applyFile(file);
                try { var dt = new DataTransfer(); dt.items.add(file); fileInput.files = dt.files; } catch (err) {}
            }
        });
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) applyFile(fileInput.files[0]);
        });

        if (clearBtn) {
            /*
             * Upload strip × button:
             * ALWAYS cancels the new file and reverts the avatar to 'original'.
             * Does NOT set removePhoto = true — the user is just undoing
             * their new upload, not removing the existing photo.
             */
            clearBtn.addEventListener('click', function (e) {
                e.stopPropagation();

                /* Clear the file input and strip */
                fileInput.value           = '';
                previewWrap.style.display = 'none';
                uploadLabel.textContent   = 'Click or drag to upload';
                uploadSub.textContent     = 'PNG · JPG · GIF · JPEG — min 10 KB, max 2 MB';
                zone.classList.remove('has-file');
                resetField(fileInput, fileError, fileServer);

                /* Revert avatar circle to original */
                restoreAvatarToOriginal();

                /* Reset removePhoto flag — cancelling upload ≠ removing photo */
                if (removeFlag) removeFlag.value = 'false';

                /* Update photo state and change detection */
                photoState   = 'original';
                photoChanged = false;       /* back to original → no change */
                updateConfirmCard();
            });
        }

        function applyFile(file) {
            var avatarImg   = document.getElementById('up-avatar-img');
            var avatarInit  = document.getElementById('up-avatar-initials');
            var removeBtn   = document.getElementById('up-avatar-remove');

            var allowed = ['image/png', 'image/jpeg', 'image/gif', 'image/jpg'];
            var isValid = true, message = '';
            if (!allowed.includes(file.type)) { isValid = false; message += 'Only PNG, JPG, GIF or JPEG allowed.<br>'; }
            if (file.size > 2 * 1024 * 1024)  { isValid = false; message += 'File must be under 2 MB.<br>'; }
            if (file.size < 10 * 1024)         { isValid = false; message += 'File must be above 10 KB.<br>'; }
            if (!isValid) {
                showError(fileInput, fileError, message, fileServer);
                fileInput.value = ''; previewWrap.style.display = 'none'; return;
            }
            showValid(fileInput, fileError, 'Image looks good!', fileServer);

            /* Cancel any pending "remove photo" since user is uploading instead */
            if (removeFlag) removeFlag.value = 'false';

            photoState   = 'new';
            photoChanged = true;
            updateConfirmCard();

            var reader = new FileReader();
            reader.onload = function (ev) {
                var url = ev.target.result;

                /* Update upload strip */
                previewImg.src            = url;
                previewName.textContent   = file.name;
                previewSize.textContent   = fmtBytes(file.size);
                previewWrap.style.display = 'flex';
                uploadLabel.textContent   = 'Image selected';
                uploadSub.textContent     = file.name;
                zone.classList.add('has-file');

                /*
                 * Update avatar circle to show the new file.
                 * If avatarImg doesn't exist yet (original had no photo),
                 * create it and hide initials.
                 */
                if (avatarImg) {
                    avatarImg.src           = url;
                    avatarImg.style.display = 'block';
                    if (avatarInit) avatarInit.style.display = 'none';
                } else if (avatarInit) {
                    var img      = document.createElement('img');
                    img.src      = url;
                    img.id       = 'up-avatar-img';
                    img.className = 'up-avatar-img';
                    img.alt      = 'Profile photo';
                    avatarInit.parentNode.insertBefore(img, avatarInit);
                    avatarInit.style.display = 'none';
                }

                /*
                 * Show the avatar × badge even if it wasn't there before
                 * (e.g. user had no photo) so they can cancel the new upload.
                 * The badge will revert to original, not set removePhoto.
                 */
                if (removeBtn) {
                    removeBtn.style.display = 'flex';
                } else {
                    /* Create the badge dynamically if original had no photo */
                    var newBtn        = document.createElement('button');
                    newBtn.type       = 'button';
                    newBtn.className  = 'up-avatar-remove';
                    newBtn.id         = 'up-avatar-remove';
                    newBtn.title      = 'Remove photo';
                    newBtn.innerHTML  = '<i class="fa-solid fa-xmark"></i>';
                    var avatarWrap    = document.querySelector('.up-avatar-wrap');
                    if (avatarWrap) avatarWrap.appendChild(newBtn);
                    /* Wire it up — the initRemovePhoto listener won't cover
                       dynamically created buttons, so attach directly */
                    newBtn.addEventListener('click', handleAvatarRemoveClick);
                }
            };
            reader.readAsDataURL(file);
        }

        function fmtBytes(b) {
            if (b < 1024)        return b + ' B';
            if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB';
            return (b / (1024 * 1024)).toFixed(1) + ' MB';
        }
    }

    /* ══════════════════════════════════════════════════════
       AVATAR × BADGE CLICK HANDLER (shared logic)
       ═══════════════════════════════════════════════════════
       What the × badge does depends on photoState:

       State 'original':
         → User wants to remove their existing server photo.
         → Set removePhoto = true, show initials, hide badge.
         → photoState = 'removed'

       State 'new':
         → User uploaded a new file but changed their mind.
         → Cancel the upload: clear file input + strip,
           revert avatar to original, reset removePhoto.
         → photoState = 'original'

       State 'removed':
         → Badge is hidden in this state, so this can't fire.
       ══════════════════════════════════════════════════════ */
    function handleAvatarRemoveClick() {
        var removeFlag  = document.getElementById('up-remove-photo-flag');
        var fileInput   = document.getElementById('up-file-input');
        var previewWrap = document.getElementById('up-file-preview');
        var uploadLabel = document.getElementById('up-upload-label');
        var uploadSub   = document.getElementById('up-upload-sub');
        var zone        = document.getElementById('up-upload-zone');
        var fileError   = document.getElementById('multipartFileError');
        var fileServer  = document.getElementById('multipartFileErrorServerSide');
        var removeBtn   = document.getElementById('up-avatar-remove');
        var avatarImg   = document.getElementById('up-avatar-img');
        var avatarInit  = document.getElementById('up-avatar-initials');

        if (photoState === 'new') {
            /*
             * User uploaded a new file and now wants to cancel it.
             * Revert to original — do NOT set removePhoto = true.
             */
            if (fileInput)   fileInput.value           = '';
            if (previewWrap) previewWrap.style.display  = 'none';
            if (uploadLabel) uploadLabel.textContent    = 'Click or drag to upload';
            if (uploadSub)   uploadSub.textContent      = 'PNG · JPG · GIF · JPEG — min 10 KB, max 2 MB';
            if (zone)        zone.classList.remove('has-file');
            resetField(fileInput, fileError, fileServer);

            if (removeFlag) removeFlag.value = 'false';

            restoreAvatarToOriginal();

            photoState   = 'original';
            photoChanged = false;

        } else if (photoState === 'original' && ORIGINAL.image !== '') {
            /*
             * User wants to remove the existing server photo.
             */
            if (removeFlag) removeFlag.value = 'true';

            if (avatarImg)  avatarImg.style.display  = 'none';
            if (avatarInit) avatarInit.style.display  = 'flex';
            else {
                /* No initials span exists — create one */
                var span        = document.createElement('span');
                span.className  = 'up-avatar-initials';
                span.textContent = '?';
                if (avatarImg) avatarImg.parentNode.insertBefore(span, avatarImg.nextSibling);
            }
            if (removeBtn) removeBtn.style.display = 'none';

            photoState   = 'removed';
            photoChanged = true;
        }

        updateConfirmCard();
    }

    /* ══════════════════════════════════════════════════════
       2. REMOVE EXISTING PHOTO (wires the avatar × badge)
       ══════════════════════════════════════════════════════ */
    function initRemovePhoto() {
        var removeBtn  = document.getElementById('up-avatar-remove');
        var removeFlag = document.getElementById('up-remove-photo-flag');

        if (!removeBtn || !removeFlag) return;

        removeBtn.addEventListener('click', handleAvatarRemoveClick);
    }

    /* ══════════════════════════════════════════════════════
       3. PASSWORD TOGGLE
       ══════════════════════════════════════════════════════ */
    function initPasswordToggle() {
        var input = document.getElementById('up-current-password');
        var btn   = document.getElementById('up-toggle-current-pw');
        var icon  = document.getElementById('up-current-pw-eye');
        if (!input || !btn || !icon) return;
        btn.addEventListener('click', function () {
            if (input.type === 'password') { input.type = 'text'; icon.className = 'fa-regular fa-eye-slash'; }
            else                           { input.type = 'password'; icon.className = 'fa-regular fa-eye'; }
        });
    }

    /* ══════════════════════════════════════════════════════
       4. BIO COUNTER
       ══════════════════════════════════════════════════════ */
    function initBioCounter() {
        var bio     = document.getElementById('up-bio');
        var counter = document.getElementById('bioCharCount');
        if (!bio || !counter) return;
        counter.textContent = bio.value.length;
    }

    /* ══════════════════════════════════════════════════════
       5. CHANGE DETECTION
       ══════════════════════════════════════════════════════ */
    function initChangeDetection() {
        ['up-name', 'up-email', 'up-bio'].forEach(function (id) {
            var el = document.getElementById(id);
            if (!el) return;
            el.addEventListener('input', updateConfirmCard);
        });
        updateConfirmCard();
    }

    /* ══════════════════════════════════════════════════════
       6. SUBMIT GUARD
       ══════════════════════════════════════════════════════ */
    function initSubmitGuard() {
        var form    = document.getElementById('up-form');
        var pwInput = document.getElementById('up-current-password');
        var pwError = document.getElementById('currentPasswordError');

        if (!form || !pwInput) return;

        form.addEventListener('submit', function (e) {
            if (!hasChanges()) return;

            if (pwInput.value.trim() === '') {
                e.preventDefault();
                showError(pwInput, pwError,
                    'Please enter your current password to save changes.', null);
                pwInput.focus();
                var confirmCard = document.getElementById('up-confirm-card');
                if (confirmCard) confirmCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    /* ══════════════════════════════════════════════════════
       7. SUBMIT LOADER
       ══════════════════════════════════════════════════════ */
    function initSubmitLoader() {
        var form = document.getElementById('up-form');
        var btn  = document.getElementById('up-submit-btn');
        if (!form || !btn) return;
        form.addEventListener('submit', function () {
            setTimeout(function () {
                var pwInput = document.getElementById('up-current-password');
                var blocked = hasChanges() && pwInput && pwInput.value.trim() === '';
                if (!blocked) {
                    btn.classList.add('loading');
                    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i><span>Saving…</span>';
                }
            }, 60);
        });
    }

    /* ══════════════════════════════════════════════════════
       8. FLASH DISMISS (4 s)
       ══════════════════════════════════════════════════════ */
    function initFlashDismiss() {
        document.querySelectorAll('.up-flash').forEach(function (el) {
            setTimeout(function () {
                el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                el.style.opacity = '0'; el.style.transform = 'translateY(-8px)';
                setTimeout(function () { if (el.parentNode) el.parentNode.removeChild(el); }, 520);
            }, 4000);
        });
    }

    /* ══════════════════════════════════════════════════════
       9. SERVER-SIDE ERROR DISMISS (5 s)
       ══════════════════════════════════════════════════════ */
    function initServerSideDismiss() {
        ['nameErrorServerSide', 'emailErrorServerSide', 'bioErrorServerSide',
         'currentPasswordErrorServerSide', 'multipartFileErrorServerSide', 'up-global-error'
        ].forEach(function (id) {
            var el = document.getElementById(id);
            if (!el || el.innerText.trim() === '') return;
            setTimeout(function () {
                el.style.transition = 'opacity 0.5s';
                el.style.opacity = '0';
                setTimeout(function () { el.style.display = 'none'; }, 500);
            }, 5000);
        });
    }

})();


/* ════════════════════════════════════════════════════════════
   FIELD VALIDATIONS — outside IIFE
   ════════════════════════════════════════════════════════════ */

// ── NAME ──────────────────────────────────────────────────
const nameInput      = document.getElementById('up-name');
const nameError      = document.getElementById('nameError');
const nameServerSide = document.getElementById('nameErrorServerSide');
let debounceTimerName;

nameInput.addEventListener('input', function () {
    const val = this.value;
    clearTimeout(debounceTimerName);
    if (val === '') { resetField(nameInput, nameError, nameServerSide); return; }
    debounceTimerName = setTimeout(() => {
        let isValid = true, message = '';
        if (!/^[A-Za-z]+( [A-Za-z]+)*$/.test(val)) { isValid = false; message += 'Only letters — single spaces between words, no trailing space.<br>'; }
        if (val.length < 3)  { isValid = false; message += 'At least 3 characters required.<br>'; }
        if (val.length > 50) { isValid = false; message += 'Max 50 characters.<br>'; }
        isValid
            ? showValid(nameInput, nameError, 'Looks good!', nameServerSide)
            : showError(nameInput, nameError, message, nameServerSide);
    }, 400);
});

// ── EMAIL ─────────────────────────────────────────────────
const emailInput      = document.getElementById('up-email');
const emailError      = document.getElementById('emailError');
const emailServerSide = document.getElementById('emailErrorServerSide');
let debounceTimerEmail;

emailInput.addEventListener('input', function () {
    const val = this.value.trim();
    clearTimeout(debounceTimerEmail);
    if (val === '') { resetField(emailInput, emailError, emailServerSide); return; }
    debounceTimerEmail = setTimeout(() => {
        let isValid = true, message = '';
        if (!/^[A-Za-z0-9._%+\-]+@gmail\.com$/i.test(val)) { isValid = false; message += 'Must be a valid @gmail.com address.<br>'; }
        if (val.length > 300) { isValid = false; message += 'Max 300 characters.<br>'; }
        isValid
            ? showValid(emailInput, emailError, 'Email looks good!', emailServerSide)
            : showError(emailInput, emailError, message, emailServerSide);
    }, 400);
});

// ── BIO ───────────────────────────────────────────────────
const bioInput      = document.getElementById('up-bio');
const bioError      = document.getElementById('bioError');
const bioServerSide = document.getElementById('bioErrorServerSide');
let debounceTimerBio;

bioInput.addEventListener('input', function () {
    const val     = this.value;
    const counter = document.getElementById('bioCharCount');
    if (counter) counter.textContent = val.length;
    clearTimeout(debounceTimerBio);
    if (val === '') { resetField(bioInput, bioError, bioServerSide); return; }
    debounceTimerBio = setTimeout(() => {
        val.length > 200
            ? showError(bioInput, bioError, 'Bio must not exceed 200 characters.<br>', bioServerSide)
            : showValid(bioInput, bioError, 'Looking good! (' + val.length + '/200)', bioServerSide);
    }, 400);
});

// ── CURRENT PASSWORD ──────────────────────────────────────
const currentPwInput  = document.getElementById('up-current-password');
const currentPwError  = document.getElementById('currentPasswordError');
const currentPwServer = document.getElementById('currentPasswordErrorServerSide');

currentPwInput.addEventListener('input', function () {
    if (this.value.trim() !== '') resetField(currentPwInput, currentPwError, currentPwServer);
});

/* ════════════════════════════════════════════════════════════
   HELPERS
   ════════════════════════════════════════════════════════════ */
function showError(input, errorEl, msg, serverSide) {
    input.classList.add('is-invalid'); input.classList.remove('is-valid');
    if (errorEl) { errorEl.style.color = '#f87171'; errorEl.innerHTML = msg; }
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}
function showValid(input, errorEl, msg, serverSide) {
    input.classList.add('is-valid'); input.classList.remove('is-invalid');
    if (errorEl) { errorEl.style.color = '#34d399'; errorEl.innerHTML = msg; }
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}
function resetField(input, errorEl, serverSide) {
    input.classList.remove('is-valid', 'is-invalid');
    if (errorEl) { errorEl.innerHTML = ''; errorEl.style.color = ''; }
    if (serverSide) { serverSide.innerHTML = ''; serverSide.style.display = 'none'; }
}