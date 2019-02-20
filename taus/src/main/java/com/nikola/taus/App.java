package com.nikola.taus;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.nikola.taus.view.MainFrame;

public class App 
{
    public static void main( String[] args )
    {
    	ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    	    	
    	MainFrame mainFrame = (MainFrame) ctx.getBean("mainFrame", MainFrame.class);
    	
    	mainFrame.init();
    }
}
