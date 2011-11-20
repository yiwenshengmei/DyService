package com.zj.osgi.dyservice.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Logger;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.zj.osgi.dyservice.ProxyProcessor;
import com.zj.osgi.dyservice.UpdateHandler;
import com.zj.osgi.dyservice.UpdateManager;

/**
 * 提供动态更新管理的核心类。
 * <p>典型用法一，将开发好的服务类注册为能够支持动态更新的服务类：
 * <pre>
 * UpdateHandler yourService = new YourService();
 * ServiceReference umsr = ctx.getServiceReference(UpdateManager.class.getName());
 * UpdateManager um = (UpdateManager)ctx.getService(umsr);
 * Object proxy = um.proxy(yourService);
 * ServiceRegistration sr = um.registerService("servicename", proxy, null);
 * </pre>
 * <p>典型用法二，动态更新服务
 * <pre>
 * String oldServiceName  = "在当前osgi环境中需要被替换的服务的名字";
 * String newRegisterName = "替换上的新服务在osgi环境中的名字"; // 通常newRegisterName = oldServiceName
 * UpdateHandler newService = new YourNewService();
 * ServiceReference umsr = ctx.getServiceReference(UpdateManager.class.getName());
 * UpdateManager um = (UpdateManager)ctx.getService(umsr);
 * um.update(oldServiceName, newService, newRegisterName, null, ctx);
 * </pre>
 * <p>
 * @author jzhao
 */
public class UpdateManagerImpl implements UpdateManager {
	public static String UPDATE 	= "UPDATE";
	public static String UPDATE_END = "UPDATEEND";
	private Map<String, Object> proxyMapper;
	private BundleContext bundleContext;
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public UpdateManagerImpl(BundleContext ctx) {
		proxyMapper = new HashMap<String, Object>();
		this.bundleContext = ctx;
	}
	
	public void setProxymapper(Map<String, Object> mapper) {
		this.proxyMapper = mapper;
	}
	
	@Override
	public void update(String oldServiceName, UpdateHandler newService, 
			String newRegisterName,	Dictionary newRegisterParam) {
		// 得到代理对象
		Object proxy = Util.getService(oldServiceName);
		// 得到代理处理类对象
		ProxyProcessor processor = 
			(ProxyProcessor)Proxy.getInvocationHandler(proxy);
		// 得到受控对象
		UpdateHandler target = processor.getTarget();
		// 发布更新开始通知
		log.info("发布更新开始通知");
		sendUpdateEvent(target);
		// 切断客户端发来的任何调用
		log.info("禁止客户端调用受控对象");
		processor.setPermission(false);
		// 等待计数器归零
		log.info("等待计数器归零");
		processor.waitForTransactionStop();
		// 通知受控对象停止工作
		log.info("通知受控对象停止工作");
		target.onEndWork();
		// 通知受控对象收集状态
		Object state = target.onGetState();
		// 注销旧服务
		log.info("注销旧服务");
		target.onUnregister();
		// 转移状态至新服务
		log.info("转移状态至新服务");
		newService.onSetState(state);
		// 注册新服务
		log.info("注册新服务");
		bundleContext.registerService(newRegisterName, 
				proxy(newService), newRegisterParam);
		// 告知新服务开始运作
		log.info("通知新服务开始运作");
		newService.onActive();
		// 发布更新结束通知
		log.info("发布更新结束通知");
		sendUpdateEndEvent(newService);
	}
	
	@Override
	public Object proxy(UpdateHandler target) {
		ProxyProcessor processor = new ProxyProcessor(target);
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), 
				target.getClass().getInterfaces(), (InvocationHandler)processor);
	}
	
	@Override
	public ServiceRegistration registerService(String registerName, Object target, Dictionary param) {
		proxyMapper.put(registerName, target);
		return bundleContext.registerService(registerName, target, param);
	}
	
	@Override
	public Map<String, Object> getAllProxyService() {
		return proxyMapper;
	}
	
	@Override
	public Object getProxyService(String servicename) {
		return proxyMapper.get(servicename);
	}
	
	private void sendUpdateEvent(Object sender) {
		sendEvent(generateTopicName(sender.getClass(), UPDATE));
	}
	
	private void sendUpdateEndEvent(Object sender) {
		sendEvent(generateTopicName(sender.getClass(), UPDATE_END));
	}
	
	private void sendEvent(String topic) {
		Map<String, String> emptyMap = new HashMap<String, String>();
		Event event = new Event(topic, emptyMap);
		getEventAdminSerivce().sendEvent(event);
	}
	
	private EventAdmin getEventAdminSerivce() {
		return Util.getService(EventAdmin.class);
	}
	
	public static String generateTopicName(Class<?> clazz, String eventType) {
		return clazz.isInterface() ? 
				clazz.getName().replace('.', '/') + "/" + eventType : 
				clazz.getInterfaces()[0].getName().replace('.', '/') + "/" + eventType;
	}
}
