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
        initLiveValidation();
        initSubmitLoader();
        initFlashDismiss();
    });

    /* ══════════════════════════════════════════════════════
       1. FILE UPLOAD — drag-drop zone with live preview
          Connects the custom zone to the hidden native input
          that th:field="*{multipartFile}" generates.
          Thymeleaf sets id="multipartFile" on that input.
       ══════════════════════════════════════════════════════ */
    function initFileUpload() {
        /* th:field="*{multipartFile}" renders id="multipartFile" */
        var fileInput   = document.getElementById('multipartFile');
        var zone        = document.getElementById('ac-upload-zone');
        var previewWrap = document.getElementById('ac-preview-wrap');
        var previewImg  = document.getElementById('ac-img-preview');
        var previewName = document.getElementById('ac-preview-name');
        var previewSize = document.getElementById('ac-preview-size');
        var uploadLabel = document.getElementById('ac-upload-label');
        var uploadSub   = document.getElementById('ac-upload-sub');
        var clearBtn    = document.getElementById('ac-clear-file');

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
            if (file && file.type.startsWith('image/')) {
                applyFile(file);
                /* Transfer dropped file to the real input via DataTransfer */
                try {
                    var dt = new DataTransfer();
                    dt.items.add(file);
                    fileInput.files = dt.files;
                } catch (err) {
                    /* DataTransfer not supported in some browsers — file won't submit but preview works */
                }
            }
        });

        /* ── Native input change ────────────────────────── */
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files[0]) {
                applyFile(fileInput.files[0]);
            }
        });

        /* ── Clear button ───────────────────────────────── */
        clearBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            resetZone();
        });

        /* ── Apply file to UI ───────────────────────────── */
        function applyFile(file) {
            var reader = new FileReader();
            reader.onload = function (ev) {
                previewImg.src = ev.target.result;
                previewName.textContent = file.name;
                previewSize.textContent = formatBytes(file.size);
                previewWrap.style.display = 'flex';
                uploadLabel.textContent = 'Image selected';
                uploadSub.textContent   = file.name;
                zone.classList.add('has-file');
            };
            reader.readAsDataURL(file);
        }

        /* ── Reset zone ─────────────────────────────────── */
        function resetZone() {
            fileInput.value        = '';
            previewImg.src         = '';
            previewWrap.style.display = 'none';
            uploadLabel.textContent = 'Click or drag image here';
            uploadSub.textContent   = 'PNG · JPG · WEBP — max 5 MB';
            zone.classList.remove('has-file');
        }

        /* ── Format file size ───────────────────────────── */
        function formatBytes(bytes) {
            if (bytes < 1024)             return bytes + ' B';
            if (bytes < 1024 * 1024)      return (bytes / 1024).toFixed(1) + ' KB';
            return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
        }
    }

    /* ══════════════════════════════════════════════════════
       2. LIVE VALIDATION
       ══════════════════════════════════════════════════════ */
    function initLiveValidation() {
        var rules = [
            {
                id:      'name',
                errId:   'nameError',
                test:    function (v) { return v.length > 0 && v.length < 2; },
                msg:     'Name must be at least 2 characters.'
            },
            {
                id:      'email',
                errId:   'emailError',
                test:    function (v) { return v.length > 0 && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v); },
                msg:     'Enter a valid email address.'
            },
            {
                id:      'phone',
                errId:   'phoneError',
                test:    function (v) { return v.length > 0 && !/^[\d\s+\-().]{7,}$/.test(v); },
                msg:     'Enter a valid phone number.'
            }
        ];

        rules.forEach(function (rule) {
            var input  = document.getElementById(rule.id);
            var errEl  = document.getElementById(rule.errId);
            if (!input || !errEl) return;

            function validate() {
                var val = input.value.trim();
                var err = rule.test(val) ? rule.msg : '';
                errEl.textContent = err;
                input.classList.toggle('is-invalid', !!err);
            }

            input.addEventListener('blur',  validate);
            input.addEventListener('input', validate);
        });
    }

    /* ══════════════════════════════════════════════════════
       3. SUBMIT LOADER
       ══════════════════════════════════════════════════════ */
    function initSubmitLoader() {
        var form = document.getElementById('ac-form');
        var btn  = document.getElementById('ac-submit-btn');
        if (!form || !btn) return;

        form.addEventListener('submit', function () {
            /* Small delay to let browser-side validation fire first */
            setTimeout(function () {
                btn.classList.add('loading');
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i><span>Saving…</span>';
            }, 50);
        });
    }

    /* ══════════════════════════════════════════════════════
       4. AUTO-DISMISS FLASH MESSAGES (4 s)
       ══════════════════════════════════════════════════════ */
    function initFlashDismiss() {
        var flashes = document.querySelectorAll('.ac-flash');
        flashes.forEach(function (el) {
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