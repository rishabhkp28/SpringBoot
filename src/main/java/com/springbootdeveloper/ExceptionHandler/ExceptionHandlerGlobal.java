package com.springbootdeveloper.ExceptionHandler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.springbootdeveloper.Exceptions.UserNotFoundException;

@ControllerAdvice
public class ExceptionHandlerGlobal {
	
	@ExceptionHandler(UserNotFoundException.class)
	public String handleException(UserNotFoundException ex) {
	    return "redirect:/login";
	}
}

