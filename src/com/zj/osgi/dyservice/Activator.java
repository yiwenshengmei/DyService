package com.zj.osgi.dyservice;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.zj.osgi.dyservice.impl.UpdateManagerImpl;

public class Activator implements BundleActivator {

	public static BundleContext bundleContext;
	private List<ServiceRegistration> regs = new ArrayList<ServiceRegistration>();

	static BundleContext getContext() {
		return bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.bundleContext = bundleContext;
		ServiceRegistration reg = bundleContext.registerService(UpdateManager.class.getName(), 
				new UpdateManagerImpl(bundleContext), null);
		regs.add(reg);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.bundleContext = null;
		for (ServiceRegistration reg : regs) {
			reg.unregister();
		}
	}

}
