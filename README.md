Implemented Features:

    * Authentication and Authorization using Spring Security.
    * Controller design with custom exception handling and database integration.
    * Home page layout developed using HTML, CSS, Bootstrap, and JavaScript.

Upcoming Features:

    * User Dashboard.
    * Forgot Password module.
    * Payment Gateway integration.
    * Cloud Integration.
    * ResponseStatus Handling in controller
    * Additional enhancements and improvements.


Progress Update (1 March):

        * Implemented the base template for the Dashboard.
        * Created stub pages for initial structure.
        * Resolved case-sensitivity issues related to Gmail sign-in (uppercase/lowercase handling).
        * Currently working on modularizing the dashboard code for better structure and maintainability.


Progress Update (2–3 March):

    * Updated ContactDTO to handle both MultipartFile and fileName.
    * Created a separate controller for contact-related operations.
    * Improved workflow by refining the service layer for User, clearly separating controller and service responsibilities.
    * Split the service layer responsibilities between User and Contact.
    * Implemented addContact functionality, including the corresponding controller and service methods to save contacts in the database.


Progress Update (3–9 March):

        * Modified the logical error in the User and Contact Entity (database mapping ManyToMany to OneToMany).
        * Configured the addContact to work and display the recent results in the dashboard.
        * Modified the addContact.html to include the server side validations as well as the js Client side dynamic validations.
        * Implemented the Favourited functionality to display the favourite contacts by the * in the dashboard and also the total favourite contacts available.
        * Implemented UI fixes and enhanced the existing code in addContact.html form.

Progress Update (9–11 March):

    * Added the functionalities for the dashboard:-
         *Get count of total contacts on the TotalContacts card displayed on dashboard, and get list of all contacts on clicking that card.
         *Get count of favourite contacts on the FavouriteContacts card displayed on dashboard, and get list of all favourite contacts on clicking that card.
         *Get count of contacts added in present month on the ContactsAddedThisMonthCard displayed on dashboard, and get list of those contacts on clicking that card.
         *Get count of groups on the Groups card present on the dashboard and get the groups when clicking that card,We get several cards for respective groups and on 
            clicking those cards the contacts are appeared filterWise based on the group alloted to them. 
    * Fixed the bug in addContactForm , to display the exception if duplicated entry occures(same name,phn number and contact)
    * Fixed bug in js file that messes with the activePage on the Thymeleaf by just checking the path mapping and not exact mapping (changed ,startsWith -> ===)
    * Most ofthe dummy links has been replaced by functional page.
    * Remaining important functionalities include, Searching Contacts, importing and exporting contacts as json,, Profile Strength indicator button, Editing the contact,
      Settings for the user, displaying the mini images of contacts in the list, user change password facility , change email facility, payment gateway integration,             Implementing ResponseEntity<> in the controller layer for proper http Response status to the user and many  more ,and at the end cloud hosting. 
    
Progress Update (11–21 March):

        * Shifted the focus from session based authentication to JWT for better scalability in future, made modules to implement JWT,in the project( This took the most time, learning JWT and implementing)
        * Made appearence appealing with better animation for the logged in user.
        * Changed the theme for the project , made theme consistent to authenticated and non authenticated users.
        * Implemented the delete functionality and edit functionality for the contacts along with dynamic validations whereever needed.
        * Implemented security patches for handling non verified GET image requests and malicious delete contact requests.
        * Mini Images of the users are now available in the list along whole project.

Progress Update (21 March -10 April):

        * Enhanced the readibility of the JWT authentication module.
        * Implemented the UserProfile Updatation modules for the user to update his details into the database.
        * Implemented the UI (html+css+js) UserProfile.html UserProfileScript.js UserProfileStyle.css.
        * Implemented the ChangePassword module to securely change password into the database and simultaneously autoLogin without being logged   out.  
        
