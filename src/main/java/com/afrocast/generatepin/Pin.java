package com.afrocast.generatepin;

public class Pin {
   
	public String generatePIN() 
	{   
	    int x = (int)(Math.random() * 9);
	    x = x + 1;
	    return (x + "") + ( ((int)(Math.random()*1000)) + "" ); 
	}
}
