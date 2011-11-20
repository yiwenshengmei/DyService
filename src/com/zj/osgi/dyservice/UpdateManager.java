package com.zj.osgi.dyservice;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.zj.osgi.dyservice.impl.UpdateManagerImpl;

public interface UpdateManager {

	/**
	 * 在当前osgi环境中实施动态更新服务。
	 * @param oldServiceName 	旧服务的名称，该参数将被用作BundleContext.getServiceReference()方法的第一个参数。
	 * @param newService 		新服务的实例，必须实现了UpdateHandler接口。
	 * @param newRegisterName	新服务在注册时候的名称，该参数将被用作BundleContext.registerService()方法的第一个参数。
	 * @param newRegisterParam	注册新服务时的参数，将被用作BundleContext.registerService()方法的第三个参数。
	 * @see BundleContext#getServiceReference(String)
	 * @see BundleContext#registerService(String, Object, Dictionary)
	 */
	public abstract void update(String oldServiceName,
			UpdateHandler newService, String newRegisterName,
			Dictionary newRegisterParam);

	/**
	 * 可以为一个已经实现了UpdateHandler接口的类实现代理，以实现动态更新前必要的逻辑
	 * @param 需要被代理的服务类，该类必须实现了UpdateHandler接口
	 * @return 返回被包装过的代理类，后续可以调用registerService方法将该代理类注册为osgi服务
	 * @see UpdateManagerImpl#proxy(UpdateHandler)
	 */
	public abstract Object proxy(UpdateHandler target);

	/**
	 * 该方法等同于在调用BundleContext.registerService(name, service, param)方法之前将name和serivce
	 * 保存在UpdateManager内部。
	 * @param registerName 		服务注册名，通常为obj.getClass().getName()
	 * @param target	需要注册的服务对象，通常是调用UpdateManager.proxy()方法后的返回值
	 * @param param		同BundleContext.registerService()方法中的param
	 * @return 返回和BundleContext.registerService(name, service, param)同样的返回值
	 * @see org.osgi.framework.BundleContext#registerService(String, Object, Dictionary)
	 * @see UpdateManagerImpl#proxy(UpdateHandler)
	 */
	public abstract ServiceRegistration registerService(String registerName,
			Object target, Dictionary param);

	public abstract Map<String, Object> getAllProxyService();

	public abstract Object getProxyService(String servicename);

}