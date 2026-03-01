

//Password Toggler 
function togglePassword() {
    const input = document.getElementById('password');
    const icon = document.getElementById('toggleIcon');

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}
// ------------------ EMAIL VALIDATION ------------------

const emailInput = document.getElementById("email");
const emailError = document.getElementById("emailError");
const emailServerSide = document.getElementById("emailErrorServerSide");

let debounceTimerEmail; //to prevent rapid firing


emailInput.addEventListener("input", function() { //only listeners execute everytiem the event occurs
    const email = this.value.trim();
    clearTimeout(debounceTimerEmail);

    if (email === "") {
        reset(emailInput, emailError,emailServerSide);
        return;
    }

    debounceTimerEmail = setTimeout(() => {
        let isValid = true;
        let message = "";

       
        // Regex check
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            isValid = false;
            message += "Not a valid email format.<br>";
        }

        // Must end with @gmail.com
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            isValid = false;
            message += "Email must end with @gmail.com.<br>";
        }
		
		if (!isValid) {
		        showError(emailInput, emailError, message, emailServerSide);
		        return;   
		    }
		
		fetch(`/dynamic/validate/email/${encodeURIComponent(email)}`) //Promise
		.then(res => res.json()) //returns res.json
		.then(
			resjson => 
				{
					console.log(resjson);
					console.log(emailInput.value+" "+resjson);
					if (email !== emailInput.value.trim()) return;//preventing api delay

					    if (!resjson) {
							isValid = false;
							message = "Email already exists.<br>";
					        
					    }
					
					if (!isValid) 
					           showError(emailInput, emailError, message, emailServerSide);
					       else 
					           showValid(emailInput, emailError, "Email looks good!", emailServerSide);
				}
			
		).catch(error => 
		{
			console.error("Server Error ", error);
			
			if (!isValid) 
			           showError(emailInput, emailError, message, emailServerSide);
			       else 
			           showValid(emailInput, emailError, "Email looks good!", emailServerSide);
			}
	       )
			
			
		
    }, 500);
});




// ------------------ NAME VALIDATION ------------------


const nameInput = document.getElementById("name");
const nameError = document.getElementById("nameError");
const nameServerSide = document.getElementById("nameErrorServerSide");

let debounceTimerName;



nameInput.addEventListener("input", function() {
    const name = this.value.trim();
    clearTimeout(debounceTimerName);

    if (name === "") {
        reset(nameInput, nameError,nameServerSide);
        return;
    }

    debounceTimerName = setTimeout(() => {
        let isValid = true;
        let message = "";

        // Name cannot be empty
        if (!name) {
            isValid = false;
            message += "Name cannot be empty.<br>";
        }

        // Only letters and spaces
        if (!/^[a-zA-Z\s]+$/.test(name)) {
            isValid = false;
            message += "Name can only have letters and spaces.<br>";
        }

        // Minimum 3 characters
        if (name.length < 3) {
            isValid = false;
            message += "Name must be at least 3 characters long.<br>";
        }

        // Show the result
        if (!isValid) showError(nameInput, nameError, message,nameServerSide);
        else showValid(nameInput, nameError, "Name looks cool!",nameServerSide);
    }, 500); // debounce 500ms
});




//--------------Password Validation--------------------------------

const passwordInput = document.getElementById("password");
const passwordError = document.getElementById("passwordError");
const passwordServerSide = document.getElementById("passwordErrorServerSide");


let debounceTimerPassword;


passwordInput.addEventListener("input",function(){
		
	const password = this.value;
	clearTimeout(debounceTimerPassword);
	if(password === "")
	{
		reset(passwordInput,passwordError,passwordServerSide);
		return;
		}
	
	debounceTimerPassword = setTimeout(()=>
		{
			let isValid = true;
			let message ="";
			
			
			if (password.length < 8) {
				   isValid = false;
			       message +=  "Password must be at least 8 characters long <br>";
			        }

			        // Lowercase check
			        if (!/[a-z]/.test(password)) {
						isValid = false;
			            message +=  "Missing Lowercase character <br>";;
			        }

			        // Uppercase check
			        if (!/[A-Z]/.test(password)) {
						isValid = false;
			             message +=  "Missing Uppercase character <br>";;
			        }

			        // Digit check
			        if (!/\d/.test(password)) {
						isValid = false;
			             message +=  "Missing a number <br>";;
			        }

			        // Special character check
			        if (!/[@$!%*?&]/.test(password)) {
						isValid = false;
			            message += "Missing a special character (@$!%*?&) <br>";
			        }

					// Show the result
					
					
			        if (!isValid) 
					{
						showError(passwordInput, passwordError, message,passwordServerSide);
					}
			        else
					{
						showValid(passwordInput, passwordError, "Strong Password!!!",passwordServerSide);
					}
	
	
			
		}
		
		,500);
		}
	
)

//the checkbox is validated on the front end only


function showError(input, errorEl, msg,serverSide) {
    input.classList.add("is-invalid");
    input.classList.remove("is-valid");
    errorEl.style.color = "red";
    errorEl.innerHTML = msg;
	if (serverSide) {
	       serverSide.innerHTML = "";
	       serverSide.style.display = "none";
	   }	
}

function showValid(input, errorEl, msg,serverSide) {
    input.classList.add("is-valid");
    input.classList.remove("is-invalid");
    errorEl.style.color = "green";
    errorEl.innerHTML = msg;
	if (serverSide) {
	       serverSide.innerHTML = "";
	       serverSide.style.display = "none";
	   } // removes that remporarily as when data is sent again ..browser sends a new data form html
}

function reset(input, errorEl,serverSide) {
    input.classList.remove("is-valid", "is-invalid");
    errorEl.style.color = "";
    errorEl.innerHTML = "";
	if (serverSide) {
	       serverSide.innerHTML = "";
	       serverSide.style.display = "none";
	   } // removes that remporarily as when data is sent again ..browser sends a new data form html
	
}



//Handling the files 

