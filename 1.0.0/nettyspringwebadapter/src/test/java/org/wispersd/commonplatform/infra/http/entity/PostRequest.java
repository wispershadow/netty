package org.wispersd.commonplatform.infra.http.entity;

import java.util.Date;

public class PostRequest {
	private Date startDate;
	private Date endDate;
	private String requestId;
	private Boolean checked;
	private Long sequence;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		return "PostRequest [startDate=" + startDate + ", endDate=" + endDate
				+ ", requestId=" + requestId + ", checked=" + checked
				+ ", sequence=" + sequence + "]";
	}
}
