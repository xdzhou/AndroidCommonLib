package com.loic.common.sqliteTool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface Column 
{
	int length();
}