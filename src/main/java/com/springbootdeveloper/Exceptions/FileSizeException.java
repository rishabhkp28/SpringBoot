package com.springbootdeveloper.Exceptions;

public class FileSizeException extends RuntimeException{
	
	public FileSizeException(String message)
	{
		super(message);
	}
	public FileSizeException(String message, Throwable cause)
	{
		super(message,cause);
	}
}
