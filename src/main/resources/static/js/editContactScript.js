/* ═══════════════════════════════════════════════════════════
   SmartCM — EDIT CONTACT PAGE SCRIPT
   ═══════════════════════════════════════════════════════════ */

(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initFileUpload();
        initSelectArrow();
        initSubmitLoader();
        initFlashDismiss();
        initFavouriteStar();
        initDescriptionCounter();
        initRemovePhoto();
    });

    function initFileUpload() {
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

        // ✅ FIX: get remove flag reference
        var removeFlag  = document.getElementById('removePhotoFlag');

        if (!fileInput || !zone) return;

        zone.addEventListener('click', function (e) {
            if (e.target === clearBtn || clearBtn.contains(e.target)) return;
            fileInput.click();
        });

        zone.addEventListener('dragover', function (e) {
            e.preventDefault();
            zone.classList.add('drag-over');
        });

        zone.addEventListener('dragleave', function (e) {
            if (!zone.contains(e.relatedTarget)) zone.classList.remove('drag-over');
        });

        zone.addEventListener('drop', function (e) {
            e.preventDefault();
            zone.classList.remove('drag-over');
            var file = e.dataTransfer.files[0];
            if (file) {

                // ✅ FIX: reset remove flag when new file dropped
                if (removeFlag) removeFlag.value = 'false';

                validateAndApplyFile(file, fileInput, fileError, fileServer,
                    previewImg, previewName, previewSize, previewWrap, uploadLabel, uploadSub, zone);

                try {
                    var dt = new DataTransfer();
                    dt.items.add(file);
                    fileInput.files = dt.files;
                } catch (err) {}
            }
        });

        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {

                // ✅ FIX: reset remove flag when new file selected
                if (removeFlag) removeFlag.value = 'false';

                validateAndApplyFile(fileInput.files[0], fileInput, fileError, fileServer,
                    previewImg, previewName, previewSize, previewWrap, uploadLabel, uploadSub, zone);
            }
        });

        clearBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            fileInput.value           = '';
            previewImg.src            = '';
            previewWrap.style.display = 'none';
            uploadLabel.textContent   = 'Click or drag image here';
            uploadSub.textContent     = 'PNG · JPG · WEBP — max 5 MB';
            zone.classList.remove('has-file');
            reset(fileInput, fileError, fileServer);
        });

        function validateAndApplyFile(file, input, errorEl, serverEl,
            imgEl, nameEl, sizeEl, wrapEl, labelEl, subEl, zoneEl) {

            var allowed = ['image/png', 'image/jpeg', 'image/gif' , 'image/jpg'];
            var isValid = true;
            var message = '';

            if (!allowed.includes(file.type)) {
                isValid   = false;
                message  += 'Only PNG, JPG, GIF or JPEG images allowed.<br>';
            }
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
                input.value          = '';
                wrapEl.style.display = 'none';
                return;
            }

            showValid(input, errorEl, 'File looks good!', serverEl);

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
        updateStar();
    }

    function initDescriptionCounter() {
        var desc    = document.getElementById('description');
        var counter = document.getElementById('descriptionCharCount');
        if (!desc || !counter) return;
        counter.textContent = desc.value.length;
    }

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

    function initRemovePhoto() {
        var removeBtn    = document.getElementById('ec-remove-photo-btn');
        var currentStrip = document.getElementById('ec-current-photo');
        var removeFlag   = document.getElementById('removePhotoFlag');

        if (!removeBtn || !currentStrip || !removeFlag) return;

        removeBtn.addEventListener('click', function () {
            removeFlag.value = 'true';
            currentStrip.style.display = 'none';
        });
    }

})();