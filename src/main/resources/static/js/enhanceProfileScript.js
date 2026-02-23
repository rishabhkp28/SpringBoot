document.addEventListener("DOMContentLoaded", function () { //although this is not needed for me as i have declared js at last in html ,its there for safety 

    const fileInput = document.getElementById("profileFile");
    const clearBtn = document.getElementById("clearFileBtn");

    if (!fileInput || !clearBtn) return;

    fileInput.addEventListener("change", function () {
        clearBtn.style.display = fileInput.files.length > 0 ? "block" : "none";
    });

    clearBtn.addEventListener("click", function () {
        fileInput.value = "";
        clearBtn.style.display = "none";
    });

});
