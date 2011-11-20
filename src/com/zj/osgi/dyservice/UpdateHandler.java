package com.zj.osgi.dyservice;

public interface UpdateHandler {
	public void onEndWork();
	public Object onGetState();
	public void onSetState(Object state);
	public void onActive();
	public void onUnregister();
}
