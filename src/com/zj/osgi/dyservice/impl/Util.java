package com.zj.osgi.dyservice.impl;

import org.osgi.framework.ServiceReference;

import com.zj.osgi.dyservice.Activator;

public class Util {
	
	public static <T> T getService(Class<T> clazz) {
		ServiceReference ref = Activator.bundleContext.getServiceReference(clazz.getName());
		return ref == null ? null : (T) Activator.bundleContext.getService(ref);
	}
	
	public static Object getService(String name) {
		ServiceReference ref = Activator.bundleContext.getServiceReference(name);
		return ref == null ? null : Activator.bundleContext.getService(ref);
	}
	
	public static void sleep(long millis) {
		try { Thread.sleep(millis); } 
		catch (InterruptedException e) { Thread.currentThread().interrupt(); }
	}
}
