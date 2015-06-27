package com.loic.common;

public class FailException extends Exception 
{

    private static final long serialVersionUID = 1L;

    public FailException(String msg)
	{
		super(msg);
	}
}
