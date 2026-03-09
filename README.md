Implemented Features:
Authentication and Authorization using Spring Security.
Controller design with custom exception handling and database integration.
Home page layout developed using HTML, CSS, Bootstrap, and JavaScript.

Upcoming Features:
User Dashboard.
Forgot Password module.
Payment Gateway integration.
Cloud Integration.
ResponseStatus Handling in controller
Additional enhancements and improvements.


Progress Update (1 March):
Implemented the base template for the Dashboard.
Created stub pages for initial structure.
Resolved case-sensitivity issues related to Gmail sign-in (uppercase/lowercase handling).
Currently working on modularizing the dashboard code for better structure and maintainability.


Progress Update (2–3 March):
Updated ContactDTO to handle both MultipartFile and fileName.
Created a separate controller for contact-related operations.
Improved workflow by refining the service layer for User, clearly separating controller and service responsibilities.
Split the service layer responsibilities between User and Contact.
Implemented addContact functionality, including the corresponding controller and service methods to save contacts in the database.


Progress Update (3–9 March):
Modified the logical error in the User and Contact Entity (database mapping ManyToMany to OneToMany).
Configured the addContact to work and display the recent results in the dashboard.
Modified the addContact.html to include the server side validations as well as the js Client side dynamic validations.
Implemented the Favourited functionality to display the favourite contacts by the * in the dashboard and also the total favourite contacts available.
Implemented UI fixes and enhanced the existing code in addContact.html form.




