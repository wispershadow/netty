package org.wispersd.commplatform.infra.http.client.pool;

public enum HandleResourcePoolFullStrategy {
	THROW_EXCEPTION,
	CREATE_UNPOOLED;
	//WAIT_IDLE
}
