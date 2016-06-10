package org.wispersd.commplatform.infra.http.client.pool;

public interface ResourcePool<T> {

	public abstract void init();

	public abstract void destroy();

	public abstract T acquireResource() throws ResourcePoolException;

	public abstract void releaseResource(T resource, boolean forcePhysicalClose)
			throws ResourcePoolException;

}