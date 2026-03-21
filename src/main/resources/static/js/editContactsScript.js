/* ═══════════════════════════════════════════════════════════
   SmartCM — EDIT CONTACTS PAGE SCRIPT
   Row hover effects · Delete confirmation · Error message fade
   ═══════════════════════════════════════════════════════════ */

// Helper: Select all elements matching a selector
const querySelectorAll = (selector) => document.querySelectorAll(selector);

// Initialize row border effects on hover
function initContactRowEffects() {
    const rows = querySelectorAll('.dash-contact-row');
    
    rows.forEach(row => {
        row.addEventListener('mouseenter', () => {
            row.style.borderLeftWidth = '2px';
            row.style.borderLeftStyle = 'solid';
            row.style.borderLeftColor = 'rgba(37, 99, 235, 0.45)';
        });
        
        row.addEventListener('mouseleave', () => {
            row.style.borderLeftWidth = '0';
        });
    });
}

// Initialize ripple effect on action buttons
function initRippleEffect() {
    const actionButtons = querySelectorAll('.dash-action-btn');
    
    actionButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            const rect = button.getBoundingClientRect();
            const ripple = document.createElement('span');
            const size = Math.max(rect.width, rect.height);
            
            // Set ripple styles
            ripple.style.position = 'absolute';
            ripple.style.width = size + 'px';
            ripple.style.height = size + 'px';
            ripple.style.borderRadius = '50%';
            ripple.style.background = 'rgba(37, 99, 235, 0.18)';
            ripple.style.transform = 'scale(0)';
            ripple.style.left = (event.clientX - rect.left - size / 2) + 'px';
            ripple.style.top = (event.clientY - rect.top - size / 2) + 'px';
            ripple.style.pointerEvents = 'none';
            ripple.style.animation = 'dashRipple 0.5s ease forwards';
            
            // Ensure button has position context
            if (getComputedStyle(button).position === 'static') {
                button.style.position = 'relative';
            }
            button.style.overflow = 'hidden';
            
            // Add ripple to button
            button.appendChild(ripple);
            
            // Remove ripple after animation
            setTimeout(() => {
                ripple.remove();
            }, 500);
        });
    });
    
    // Add ripple animation keyframes if not already present
    if (!document.getElementById('dash-ripple-style')) {
        const styleSheet = document.createElement('style');
        styleSheet.id = 'dash-ripple-style';
        styleSheet.textContent = `
            @keyframes dashRipple {
                to {
                    transform: scale(2.5);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(styleSheet);
    }
}

// Initialize edit button click handler
function initEditButtonEffect() {
    const editButtons = querySelectorAll('.dash-edit-btn');
    
    editButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            // Prevent row highlight effects when clicking edit button
            event.stopPropagation();
        });
    });
}


// Handle contact deletion with confirmation
function handleDeleteContact(event) {
    event.preventDefault();
    
    const deleteButton = event.target.closest('.dash-delete-btn');
    if (!deleteButton) return;
    
    const contactId = deleteButton.getAttribute('data-contact-id');
    const contactName = deleteButton.getAttribute('data-contact-name');
    
    // Show confirmation dialog
    const confirmed = confirm(
        `Are you sure you want to delete "${contactName}"? This action cannot be undone.`
    );
    
    if (confirmed) {
        // Create and submit form
        const deleteForm = document.createElement('form');
        deleteForm.method = 'POST';
        deleteForm.action = '/user/deleteContact/' + contactId;
        
        document.body.appendChild(deleteForm);
        deleteForm.submit();
    }
}

// Handle error message fade-out
function initErrorMessageFadeOut() {
    const errorMessage = document.getElementById('errorMessageContact');
    
    if (errorMessage && errorMessage.textContent.trim() !== '') {
        // Fade out after 5 seconds
        setTimeout(() => {
            errorMessage.style.transition = 'opacity 0.5s ease';
            errorMessage.style.opacity = '0';
            
            // Remove element after fade animation
            setTimeout(() => {
                errorMessage.textContent = '';
                errorMessage.style.display = 'none';
            }, 500);
        }, 5000);
    }
}

// Initialize all effects when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    initContactRowEffects();
    initRippleEffect();
    initEditButtonEffect();
    initDeleteButtonEffect();
    initErrorMessageFadeOut();
    
    // Console message
    console.log(
        '%cSmartCM Edit Contacts · Page Ready',
        'color:#e8a020;font-family:serif;font-style:italic;font-size:12px'
    );
});