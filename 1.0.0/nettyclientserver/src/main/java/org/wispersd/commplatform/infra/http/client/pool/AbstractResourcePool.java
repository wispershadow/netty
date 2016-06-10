package org.wispersd.commplatform.infra.http.client.pool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResourcePool<T> implements ResourcePool<T> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourcePool.class);
	private volatile int coreSize = 5;
	private volatile int maxSize = 10;
	private volatile long maxSessionIdleTime =  60000;
	
	private static final int STATUS_IDLE = 1;
	private static final int STATUS_USED = 2;
	private final NavigableSet<PoolableResourceEntry<T>> resourcePool;
	
	private final ScheduledExecutorService scheduledExecutorService;
	private final ReentrantLock addQueueLock = new ReentrantLock();
	private volatile HandleResourcePoolFullStrategy handleResourcePoolFullStrategy = HandleResourcePoolFullStrategy.THROW_EXCEPTION;
	
	private final AtomicInteger usedCount;
	private final AtomicInteger idleCount;
	
	public AbstractResourcePool() {
		this(5, 10);
	}
	
	
	public AbstractResourcePool(int coreSize, int maxSize) {
		this.coreSize = coreSize;
		this.maxSize = maxSize;
		this.resourcePool = new ConcurrentSkipListSet<PoolableResourceEntry<T>>(new Comparator<PoolableResourceEntry<T>>() {
			public int compare(PoolableResourceEntry<T> e1, PoolableResourceEntry<T> e2) {
				int status1 = e1.status;
				int status2 = e2.status;
				if (status1 != status2) {
					return status1 - status2;
				}
				else {
					T res1 = e1.resource;
					T res2 = e2.resource;
					int code1 = (res1 == null)?0:res1.hashCode();
					int code2 = (res2 == null)?0:res2.hashCode();
					return code1-code2;
				}
			}
			
		});
		
		this.idleCount = new AtomicInteger(0);
		this.usedCount = new AtomicInteger(0);
		this.scheduledExecutorService = Executors.newScheduledThreadPool(5);
	}


	/* (non-Javadoc)
	 * @see org.wispersd.commplatform.infra.http.client.pool.ResourcePool#init()
	 */
	public void init() {
		for(int i=0; i<coreSize; i++) {
			try {
				T newRes = this.createPhysical();
				this.resourcePool.add(new PoolableResourceEntry<T>(newRes));
				idleCount.incrementAndGet();
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully created resource and add to pool");
				}
			} catch (Exception e) {
				logger.error("Error creating session during startup", e);
			}
		}
		
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				doHouseKeeping();
			}
		}, 10, 30, TimeUnit.SECONDS);
	}

	
	/* (non-Javadoc)
	 * @see org.wispersd.commplatform.infra.http.client.pool.ResourcePool#destroy()
	 */
	public void destroy() {
		scheduledExecutorService.shutdown();
		try {
			scheduledExecutorService.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		
		while(!resourcePool.isEmpty()) {
			PoolableResourceEntry<T> firstEntry = resourcePool.pollFirst();
			if (firstEntry != null) {
				try {
					performPhysicalClose(firstEntry.resource);
				} catch (Exception e) {
				}
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.wispersd.commplatform.infra.http.client.pool.ResourcePool#acquireResource()
	 */
	public T acquireResource() throws ResourcePoolException {
		T resource  = createAndAddToPool(coreSize);
		if (resource != null) {
			if (logger.isDebugEnabled()){
				logger.debug("Number of resources in pool is less than core size: {}, resource created", coreSize);
			}
			return setUsed(resource);
		}
		resource = retrieveIdleFromPool();
		if (resource != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully retrieved idle resource from pool");
			}
			return setUsed(resource);
		}
		else {
			resource = createAndAddToPool(maxSize);
			if (resource != null) {
				if (logger.isDebugEnabled()){
					logger.debug("Number of resources in pool is less than max size: {}, resource created", maxSize);
				}
				return setUsed(resource);
			}
			else {
				return handleResourcePoolFull();
			}
		}
	}
	
	
	protected T createAndAddToPool(int limit) throws ResourcePoolException {
		if (logger.isDebugEnabled()) {
			logger.debug("Begin creating new resource and add to pool with limit = {}", limit);
		}
		PoolableResourceEntry<T> resourceEntry = null;
		addQueueLock.lock();
		try {
			if (resourcePool.size() < limit) {
				resourceEntry = new PoolableResourceEntry<T>();
				resourcePool.add(resourceEntry);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Unable to create new resourceEntry, number of entries in pool: {} is more than limit: {}", resourcePool.size(), limit);
				}
				return null;
			}
		}
		finally {
			addQueueLock.unlock();
		}
		
		
		try {
			T resource = this.createPhysical();
			resourceEntry.resource = resource;
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully created new resource and add to pool");
			}
			return resource;
		}
		catch(Exception re) {
			re.printStackTrace();
			logger.error("Error creating resource", re);
			//rollback previous changes
			addQueueLock.lock();
			try {
				resourcePool.remove(resourceEntry);
			}
			finally {
				addQueueLock.unlock();
			}
		}
		return null;
	}
	
	
	protected int countIdle() {
		int freeCount = 0;
		for(PoolableResourceEntry<T> nextEntry: resourcePool) {
			if (nextEntry.status == STATUS_IDLE) {
				freeCount++;
			}
			else {
				break;
			}
		}
		return freeCount;
	}
	
	protected T retrieveIdleFromPool() throws ResourcePoolException {
		addQueueLock.lock();
		try {
			PoolableResourceEntry<T> firstEntry = resourcePool.first();
			if (firstEntry != null) {
				if (firstEntry.status == STATUS_IDLE) {
					if (logger.isDebugEnabled()) {
						logger.debug("Find idle resource from pool, trying to change resource status to used");
					}
					resourcePool.remove(firstEntry);
					firstEntry.lastAccessTime = System.currentTimeMillis();
					firstEntry.status = STATUS_USED;
					this.idleCount.decrementAndGet();
					if (resourcePool.add(firstEntry)) {
						return firstEntry.resource;
					}
					else {
						//this is extremely impossible as we locked when we poll first entry and no data could be added ,but just be safe
						//cleanSessionOnError(firstEntry.session, false);
						throw new ResourcePoolException("Unable to put accquired resource back to pool, pool is full");
					}
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("First entry in pool is used, nothing retrieved from pool");
					}
				}
			}
			return null;
		}
		finally {
			addQueueLock.unlock();
		}
	}
	
	
	protected T handleResourcePoolFull() throws ResourcePoolException{
		if (handleResourcePoolFullStrategy == HandleResourcePoolFullStrategy.THROW_EXCEPTION) {
			throw new ResourcePoolException("Resource pool is full! unable to create new resource");
		}
		else if (handleResourcePoolFullStrategy == HandleResourcePoolFullStrategy.CREATE_UNPOOLED) {
			try {
				return this.createPhysical();
			} catch (Exception e) {
				throw new ResourcePoolException(e);
			}
		}
		else {
			throw new ResourcePoolException("Unknown handleResourcePoolFullStrategy");
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.wispersd.commplatform.infra.http.client.pool.ResourcePool#releaseResource(T, boolean)
	 */
	public void releaseResource(T resource, boolean forcePhysicalClose) throws ResourcePoolException{
		boolean isRealPhysicalClose = forcePhysicalClose;
		addQueueLock.lock();
		try {
			PoolableResourceEntry<T> entry = new PoolableResourceEntry<T>(resource);
			entry.status = STATUS_USED;
			boolean removed = resourcePool.remove(entry);
			if (!removed) {
				isRealPhysicalClose = true;
				throw new ResourcePoolException("Unable to find resource in pool");
			}
			else {
				this.usedCount.decrementAndGet();
				entry.lastAccessTime = System.currentTimeMillis();
				entry.status = STATUS_IDLE;
				//clear any pending state on the resource before return it to pool
				try {
					this.refreshState(entry.resource);
				} catch (Exception e) {
				}
				
				if (!isRealPhysicalClose) {
					if (resourcePool.add(entry)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Return idle resource to pool");
						}
						this.idleCount.incrementAndGet();
					}
					else {
						//this is extremely impossible as we locked when we poll first entry and no data could be added ,but just be safe
						isRealPhysicalClose = true;
						throw new ResourcePoolException("Unable to put idle resource back to pool, pool is full");
					}
				}
			}
		}
		finally {
			addQueueLock.unlock();
			if (isRealPhysicalClose) {
				try {
					performPhysicalClose(resource);
				} catch (Exception e) {
					logger.error("Error closing physical resource", e);
				}
			}
		}
	}
	
	protected void doHouseKeeping() {
		List<T> toCleanQueue = new ArrayList<T>(100);
		addQueueLock.lock();
		try {
			boolean cleared = false;
			while(resourcePool.size() > coreSize) {
				PoolableResourceEntry<T> entry = resourcePool.first();
				if (entry != null) {
					if (entry.status == STATUS_IDLE && System.currentTimeMillis() - entry.lastAccessTime >= maxSessionIdleTime) {
						cleared = true;
						resourcePool.remove(entry);
						idleCount.decrementAndGet();
						toCleanQueue.add(entry.resource);
					}
				}
				if (!cleared) {
					break;
				}
			}
		}
		finally {
			addQueueLock.unlock();
		}
		
		for(T nextResource: toCleanQueue) {
			try {
				performPhysicalClose(nextResource);
			} catch (Exception e) {
				logger.error("Error clear session ", e);
			}
		}
		toCleanQueue.clear();
	}
	
	protected abstract T createPhysical() throws Exception;
	
	protected abstract void performPhysicalClose(T resource) throws Exception;
	
	protected abstract void refreshState(T resource) throws Exception;
	
	
	public long getMaxSessionIdleTime() {
		return maxSessionIdleTime;
	}

	public void setMaxSessionIdleTime(long maxSessionIdleTime) {
		this.maxSessionIdleTime = maxSessionIdleTime;
	}


	public int getTotalResourceCount() {
		return resourcePool.size();
	}
	
	
	public int getUsedResourceCount() {
		return this.usedCount.get();
	}
	
	public int getIdleResourceCount() {
		return this.idleCount.get();
	}

	
	public HandleResourcePoolFullStrategy getHandleResourcePoolFullStrategy() {
		return handleResourcePoolFullStrategy;
	}


	public void setHandleResourcePoolFullStrategy(HandleResourcePoolFullStrategy handleResourcePoolFullStrategy) {
		this.handleResourcePoolFullStrategy = handleResourcePoolFullStrategy;
	}

	
	protected T setUsed(T resource) {
		this.idleCount.decrementAndGet();
		this.usedCount.incrementAndGet();
		return resource;
	}

	static class PoolableResourceEntry<T> {
		volatile T resource;
		volatile long lastAccessTime;
		volatile int status; 
		
		PoolableResourceEntry(T t) {
			resource = t;
			lastAccessTime = System.currentTimeMillis();
			status = STATUS_IDLE;
		}
		
		PoolableResourceEntry() {
			lastAccessTime = System.currentTimeMillis();
			status = STATUS_USED;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((resource == null) ? 0 : resource.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PoolableResourceEntry<T> other = (PoolableResourceEntry<T>) obj;
			if (resource == null) {
				if (other.resource != null)
					return false;
			} else if (!resource.equals(other.resource))
				return false;
			return true;
		}
		
		
	}

}
