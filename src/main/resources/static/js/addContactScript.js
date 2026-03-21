/* ═══════════════════════════════════════════════════════════
   SmartCM — ADD CONTACT PAGE SCRIPT
   ═══════════════════════════════════════════════════════════
   NOTE: This script NEVER touches sidebar / profile / mobile
   drawer — those are fully handled by dashboardBaseScript.js
   which already loaded before this file.
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initFileUpload();
        initSelectArrow();
        initSubmitLoader();
        initFlashDismiss();
        initFavouriteStar();
    });

    /* ══════════════════════════════════════════════════════
       1. FILE UPLOAD — drag-drop zone with live preview
          Connects the custom zone to the hidden native input
          that th:field="*{multipartFile}" generates.
          Thymeleaf sets id="multipartFile" on that input.
       ══════════════════════════════════════════════════════ */
    function initFileUpload() {
        var fileInput      = document.getElementById('multipartFile');
        var zone           = document.getElementById('ac-upload-zone');
        var previewWrap    = document.getElementById('ac-preview-wrap');
        var previewImg     = document.getElementById('ac-img-preview');
        var previewName    = document.getElementById('ac-preview-name');
        var previewSize    = document.getElementById('ac-preview-size');
        var uploadLabel    = document.getElementById('ac-upload-label');
        var uploadSub      = document.getElementById('ac-upload-sub');
        var clearBtn       = document.getElementById('ac-clear-file');
        var fileError      = document.getElementById('fileError');
        var fileServerSide = document.getElementById('fileErrorServerSide');

        if (!fileInput || !zone) return;

        /* ── Click zone → open file picker ─────────────── */
        zone.addEventListener('click', function (e) {
            if (e.target === clearBtn || clearBtn.contains(e.target)) return;
            fileInput.click();
        });

        /* ── Drag over ──────────────────────────────────── */
        zone.addEventListener('dragover', function (e) {
            e.preventDefault();
            zone.classList.add('drag-over');
        });

        zone.addEventListener('dragleave', function (e) {
            if (!zone.contains(e.relatedTarget)) {
                zone.classList.remove('drag-over');
            }
        });

        /* ── Drop ───────────────────────────────────────── */
        zone.addEventListener('drop', function (e) {
            e.preventDefault();
            zone.classList.remove('drag-over');
            var file = e.dataTransfer.files[0];
            if (file) {
                validateAndApplyFile(file, fileInput, fileError, fileServerSide,
                    previewImg, previewName, previewSize, previewWrap, uploadLabel, uploadSub, zone);
                try {
                    var dt = new DataTransfer();
                    dt.items.add(file);
                    fileInput.files = dt.files;
                } catch (err) { /* fallback for older browsers */ }
            }
        });

        /* ── Native input change ────────────────────────── */
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {
                validateAndApplyFile(fileInput.files[0], fileInput, fileError, fileServerSide,
                    previewImg, previewName, previewSize, previewWrap, uploadLabel, uploadSub, zone);
            }
        });

        /* ── Clear button ───────────────────────────────── */
        clearBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            fileInput.value           = '';
            previewImg.src            = '';
            previewWrap.style.display = 'none';
            uploadLabel.textContent   = 'Click or drag image here';
            uploadSub.textContent     = 'PNG · JPG · WEBP — max 5 MB';
            zone.classList.remove('has-file');
            reset(fileInput, fileError, fileServerSide);
        });

        function validateAndApplyFile(file, input, errorEl, serverSide,
            imgEl, nameEl, sizeEl, wrapEl, labelEl, subEl, zoneEl) {

            var isValid      = true;
            var message      = '';
            var allowedTypes = ['image/png', 'image/jpeg', 'image/gif' , 'image/jpg'];

            if (!allowedTypes.includes(file.type)) {
                isValid  = false;
                message += 'Only PNG, JPG or WEBP images allowed.<br>';
            }

            if (file.size > 2 *1024 *1024) {
                isValid  = false;
                message += 'File must be under 2 MB.<br>';
            }
			if (file.size < 1024 * 10) {
						                isValid   = false;
						                message  += 'File must be above 10 KB.<br>';
						            }

            if (!isValid) {
                showError(input, errorEl, message, serverSide);
                input.value          = '';
                wrapEl.style.display = 'none';
                return;
            }

            showValid(input, errorEl, 'File looks good!', serverSide);

            var reader    = new FileReader();
            reader.onload = function (ev) {
                imgEl.src            = ev.target.result;
                nameEl.textContent   = file.name;
                sizeEl.textContent   = formatBytes(file.size);
                wrapEl.style.display = 'flex';
                labelEl.textContent  = 'Image selected';
                subEl.textContent    = file.name;
                zoneEl.classList.add('has-file');
            };
            reader.readAsDataURL(file);
        }

        function formatBytes(bytes) {
            if (bytes < 1024)        return bytes + ' B';
            if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
            return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
        }
    }

    /* ══════════════════════════════════════════════════════
       2. SELECT ARROW ROTATION
       ══════════════════════════════════════════════════════ */
    function initSelectArrow() {
        document.querySelectorAll('.ac-select-wrap').forEach(function (wrap) {
            var select = wrap.querySelector('select');
            var arrow  = wrap.querySelector('.ac-select-arrow');
            if (!select || !arrow) return;

            select.addEventListener('mousedown', function () { arrow.classList.add('open'); });
            select.addEventListener('change',    function () { arrow.classList.remove('open'); });
            select.addEventListener('blur',      function () { arrow.classList.remove('open'); });
        });
    }

    /* ══════════════════════════════════════════════════════
       3. FAVOURITE STAR ICON SWAP (solid ↔ regular)
       ══════════════════════════════════════════════════════ */
    function initFavouriteStar() {
        var favCheckbox = document.getElementById('favourite');
        var favStar     = document.getElementById('ac-fav-star');
        if (!favCheckbox || !favStar) return;

        function updateStar() {
            favStar.innerHTML = favCheckbox.checked
                ? '<i class="fa-solid fa-star"></i>'
                : '<i class="fa-regular fa-star"></i>';
        }
        favCheckbox.addEventListener('change', updateStar);
        updateStar(); // sync on page load (e.g. form re-render after validation error)
    }

    /* ══════════════════════════════════════════════════════
       4. SUBMIT LOADER
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
       5. AUTO-DISMISS FLASH MESSAGES (4 s)
       ══════════════════════════════════════════════════════ */
    function initFlashDismiss() {
        document.querySelectorAll('.ac-flash').forEach(function (el) {
            setTimeout(function () {
                el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                el.style.opacity    = '0';
                el.style.transform  = 'translateY(-8px)';
                setTimeout(function () {
                    if (el.parentNode) el.parentNode.removeChild(el);
                }, 520);
            }, 4000);
        });
    }

})();


/* ════════════════════════════════════════════════════════════
   DYNAMIC FIELD VALIDATIONS
   All regexes below exactly mirror the DTO @Pattern constraints
   so client-side and server-side validation never disagree.
   ════════════════════════════════════════════════════════════ */


   // ------------------ NAME VALIDATION ------------------
   // DTO: @NotBlank  @Size(min=3, max=100)  @Pattern(^[A-Za-z]+( [A-Za-z]+)*$)

   const nameInput      = document.getElementById('name');
   const nameError      = document.getElementById('nameError');
   const nameServerSide = document.getElementById('nameErrorServerSide');

   let debounceTimerName;

   nameInput.addEventListener('input', function () {

       const name = this.value;
       clearTimeout(debounceTimerName);

       if (name === '') {
           reset(nameInput, nameError, nameServerSide);
           return;
       }

       debounceTimerName = setTimeout(() => {

           let isValid = true;
           let message = '';

           // mirrors: ^[A-Za-z]+( [A-Za-z]+)*$
           if (!/^[A-Za-z]+( [A-Za-z]+)*$/.test(name)) {
               isValid  = false;
               message += 'Name can contain only letters and single spaces (not trailing) between words.<br>';
           }

           // mirrors: @Size(min = 3)
           if (name.length < 3) {
               isValid  = false;
               message += 'Name must be at least 3 characters long.<br>';
           }

           // mirrors: @Size(max = 100)
           if (name.length > 100) {
               isValid  = false;
               message += 'Name must not exceed 100 characters.<br>';
           }

           if (!isValid)
               showError(nameInput, nameError, message, nameServerSide);
           else
               showValid(nameInput, nameError, 'Looks good!', nameServerSide);

       }, 400);
   });


   // ------------------ NICKNAME VALIDATION ------------------
   // DTO: @Size(max=100)  @Pattern(^$|^[A-Za-z0-9]+( [A-Za-z0-9]+)*$)  — optional field

   const nickInput      = document.getElementById('nickName');
   const nickError      = document.getElementById('nickNameError');
   const nickServerSide = document.getElementById('nickNameErrorServerSide');

   let debounceTimerNick;

   nickInput.addEventListener('input', function () {

       const nick = this.value;
       clearTimeout(debounceTimerNick);

       if (nick === '') {
           reset(nickInput, nickError, nickServerSide);
           return;
       }

       debounceTimerNick = setTimeout(() => {

           let isValid = true;
           let message = '';

           // mirrors: ^[A-Za-z0-9]+( [A-Za-z0-9]+)*$
           if (!/^[A-Za-z0-9]+( [A-Za-z0-9]+)*$/.test(nick)) {
               isValid  = false;
               message += 'Nick name can contain only letters, digits and single spaces (not trailing) between words.<br>';
           }

           // mirrors: @Size(max = 100)
           if (nick.length > 100) {
               isValid  = false;
               message += 'Nick name must not exceed 100 characters.<br>';
           }

           if (!isValid)
               showError(nickInput, nickError, message, nickServerSide);
           else
               showValid(nickInput, nickError, 'Looks good!', nickServerSide);

       }, 400);
   });


   // ------------------ EMAIL VALIDATION ------------------
   // DTO: @Email  @Size(max=300)  @Pattern(^$|^[A-Za-z0-9._%+-]+@gmail\.com$, CASE_INSENSITIVE)  — optional field

   const emailInput      = document.getElementById('email');
   const emailError      = document.getElementById('emailError');
   const emailServerSide = document.getElementById('emailErrorServerSide');

   let debounceTimerEmail;

   emailInput.addEventListener('input', function () {

       const email = this.value.trim();
       clearTimeout(debounceTimerEmail);

       if (email === '') {
           reset(emailInput, emailError, emailServerSide);
           return;
       }

       debounceTimerEmail = setTimeout(() => {

           let isValid = true;
           let message = '';

           // mirrors: ^[A-Za-z0-9._%+-]+@gmail\.com$ (case insensitive)
           if (!/^[A-Za-z0-9._%+\-]+@gmail\.com$/i.test(email)) {
               isValid  = false;
               message += 'Email must be a valid @gmail.com address.<br>';
           }

           // mirrors: @Size(max = 300)
           if (email.length > 300) {
               isValid  = false;
               message += 'Email must not exceed 300 characters.<br>';
           }

           if (!isValid)
               showError(emailInput, emailError, message, emailServerSide);
           else
               showValid(emailInput, emailError, 'Email looks good!', emailServerSide);

       }, 400);
   });


   // ------------------ PHONE VALIDATION ------------------
   // DTO: @NotBlank  @Pattern(^[0-9]{10}$)

   const phoneInput      = document.getElementById('phone');
   const phoneError      = document.getElementById('phoneError');
   const phoneServerSide = document.getElementById('phoneErrorServerSide');

   let debounceTimerPhone;

   phoneInput.addEventListener('input', function () {

       const phone = this.value.trim();
       clearTimeout(debounceTimerPhone);

       if (phone === '') {
           reset(phoneInput, phoneError, phoneServerSide);
           return;
       }

       debounceTimerPhone = setTimeout(() => {

           let isValid = true;
           let message = '';

           // mirrors: ^[0-9]{10}$
           if (!/^[0-9]{10}$/.test(phone)) {
               isValid  = false;
               message += 'Phone number must be exactly 10 digits.<br>';
           }

           if (!isValid)
               showError(phoneInput, phoneError, message, phoneServerSide);
           else
               showValid(phoneInput, phoneError, 'Valid phone number!', phoneServerSide);

       }, 400);
   });


   // ------------------ GROUP VALIDATION ------------------
   // DTO: @NotNull

   const groupInput      = document.getElementById('group');
   const groupError      = document.getElementById('groupError');
   const groupServerSide = document.getElementById('groupErrorServerSide');

   groupInput.addEventListener('change', function () {

       const group = this.value;

       if (group === '') {
           showError(groupInput, groupError, 'Please select a group.', groupServerSide);
       } else {
           showValid(groupInput, groupError, '', groupServerSide);
       }
   });

   // ------------------ DESCRIPTION VALIDATION ------------------
   // DTO: @Size(max = 200)

   const descriptionInput      = document.getElementById('description');
   const descriptionError      = document.getElementById('descriptionError');
   const descriptionServerSide = document.getElementById('descriptionErrorServerSide');

   let debounceTimerDescription;

   descriptionInput.addEventListener('input', function () {

       const description = this.value;

       document.getElementById('descriptionCharCount').textContent = description.length;

       clearTimeout(debounceTimerDescription);

       if (description === '') {
           reset(descriptionInput, descriptionError, descriptionServerSide);
           return;
       }

       debounceTimerDescription = setTimeout(() => {

           let isValid = true;
           let message = '';

           // mirrors: @Size(max = 200)
           if (description.length > 200) {
               isValid  = false;
               message += 'Description must not exceed 200 characters.<br>';
           }

           if (!isValid)
               showError(descriptionInput, descriptionError, message, descriptionServerSide);
           else
               showValid(descriptionInput, descriptionError,
                         'Looking good! (' + description.length + '/200)', descriptionServerSide);

       }, 400);
   });


   // ------------------ COMMON FUNCTIONS ------------------

   function showError(input, errorEl, msg, serverSide) {
       input.classList.add('is-invalid');
       input.classList.remove('is-valid');
       errorEl.style.color = 'red';
       errorEl.innerHTML   = msg;
       if (serverSide) {
           serverSide.innerHTML     = '';
           serverSide.style.display = 'none';
       }
   }

   function showValid(input, errorEl, msg, serverSide) {
       input.classList.add('is-valid');
       input.classList.remove('is-invalid');
       errorEl.style.color = 'green';
       errorEl.innerHTML   = msg;
       if (serverSide) {
           serverSide.innerHTML     = '';
           serverSide.style.display = 'none';
       }
   }

   function reset(input, errorEl, serverSide) {
       input.classList.remove('is-valid', 'is-invalid');
       errorEl.innerHTML   = '';
       errorEl.style.color = '';
       if (serverSide) {
           serverSide.innerHTML     = '';
           serverSide.style.display = 'none';
       }
   }
// ------------------ AUTO HIDE DUPLICATE ERROR ------------------

window.addEventListener('DOMContentLoaded', function () {

    const duplicateError = document.getElementById('duplicateErrorServerSide');

    if (duplicateError && duplicateError.innerText.trim() !== '') {
        setTimeout(() => {
            duplicateError.style.transition = 'opacity 0.5s';
            duplicateError.style.opacity    = '0';
            setTimeout(() => {
                duplicateError.style.display = 'none';
            }, 500);
        }, 5000);
    }
});