

document.addEventListener("DOMContentLoaded", function () {

    // Enable hover dropdowns only on desktop (992px and above)
    if (window.innerWidth >= 900) 
	{
	
        const dropdowns = document.querySelectorAll(".navbar .dropdown");//calling for the ,dropdown classes (present inside navbar) menu item classes
        dropdowns.forEach(dropdown => 
		{
	        const toggle = dropdown.querySelector(".dropdown-toggle"); // gives the active status to the downward arrow(on bootstrap side)
	        const menu = dropdown.querySelector(".dropdown-menu");
	            // Show dropdown on mouse enter
	        dropdown.addEventListener("mouseenter", () => {
							        menu.classList.add("show");
									toggle.classList.add("show");
							        toggle.setAttribute("aria-expanded", "true");//aria-expanded tells the arrow that when something is active and when it is not
							        		});

            	// Hide dropdown on mouse leave
		        dropdown.addEventListener("mouseleave", () => {
				        menu.classList.remove("show");
				        toggle.classList.remove("show");
				        toggle.setAttribute("aria-expanded", "false");
				        });
		
		  });
      }
		
		    // Auto-close mobile menu after clicking any nav link or dropdown item
		    const navLinks = document.querySelectorAll('.navbar-nav .nav-link:not(.dropdown-toggle), .navbar-nav .dropdown-item');
			//select navlinks present inside navbar nav but it must not have dropdowntoggle
		    const navCollapse = document.getElementById('mainNavbar');
		    
		    if (navCollapse) 
			{
		        navLinks.forEach(link => {
		            link.addEventListener('click', () => {
		                if (window.innerWidth < 992) {
		                    const bsCollapse = bootstrap.Collapse.getInstance(navCollapse);//close the element immmediately on event occurance if its open
		                    if (bsCollapse) {
		                        bsCollapse.hide();
		                    }
		                }/*					If the menu is currently open or has been opened before, it returns the Collapse object
							If not, it returns null*/
		            });
		        });
		    }
			
			
			// Close mobile menu when clicking outside
			    document.addEventListener('click', function(event) 
				{
			        if (window.innerWidth < 992 && navCollapse) {
			            const navbar = document.querySelector('.navbar');
			            const isClickInsideNavbar = navbar.contains(event.target);
			            const bsCollapse = bootstrap.Collapse.getInstance(navCollapse);
			            
			            // If click is outside navbar and menu is open, close it
			            if (!isClickInsideNavbar && bsCollapse && navCollapse.classList.contains('show')) {
			                bsCollapse.hide();
			            }
			        }
			    });
			
	
			
});
