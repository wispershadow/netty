package org.wispersd.commplatform.infra.http.client.converters;

public interface Populator<S, T> {
	public void populate(S s, T t);

}
