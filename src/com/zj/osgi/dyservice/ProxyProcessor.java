package com.zj.osgi.dyservice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * 实现了InvocationHandler接口，用于拦截受调用受控对象方法的处理类。
 * @author jzhao
 * @version 1.0
 * @since JDK6.0
 */
public class ProxyProcessor implements InvocationHandler{
	/**
	 * 用于指示当前受控对象的public类型方法是否允许被调用。
	 */
	private boolean permission;
	/**
	 * 用于保存受控对象
	 */
	private UpdateHandler target;
	private Object transLock;
	private int transactionCnt;
	
	public int getTransactionCnt() {
		return transactionCnt;
	}

	public ProxyProcessor(UpdateHandler target) {
		this.target = target;
		transLock = new Object();
		transactionCnt = 0;
		permission = true;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (!permission)
			throw new RuntimeException("The target is updating...");
		if (Modifier.isPublic(method.getModifiers())) {
			incTransCnt();
			Object result = null;
			try { result = method.invoke(target, args);	} 
			catch (Exception ex) { throw ex; } 
			finally { descTransCnt(); }
			return result;
		} else { return method.invoke(target, args); }
	}
	
	public void setPermission(boolean flag) {
		this.permission = flag;
	}
	
	public boolean getPermission() {
		return permission;
	}
	
	private void descTransCnt() {
		synchronized(transLock) {
			transactionCnt--;
			if (transactionCnt == 0) transLock.notifyAll();
		}
	}
	
	private void incTransCnt() {
		synchronized(transLock) {
			transactionCnt++;
		}
	}
	
	public void waitForTransactionStop() {
		synchronized(transLock) { try {
			if (transactionCnt != 0) 
				transLock.wait();
		} catch (InterruptedException e) { e.printStackTrace(); }}
	}

	public UpdateHandler getTarget() {
		return target;
	}
}
