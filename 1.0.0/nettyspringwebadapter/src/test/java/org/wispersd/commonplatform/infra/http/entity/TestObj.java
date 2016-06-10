package org.wispersd.commonplatform.infra.http.entity;

public class TestObj {
	private String a;
	private int b;
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public int getB() {
		return b;
	}
	public void setB(int b) {
		this.b = b;
	}
	@Override
	public String toString() {
		return "TestObj [a=" + a + ", b=" + b + "]";
	}
	
	
}
