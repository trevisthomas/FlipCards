package org.ttdc.flipcards.server;

import java.io.Serializable;

public class CacheKeyStudyItemMeta implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8263349783748758155L;
	private String studyItemId;
	private String ownerId;
	
	public CacheKeyStudyItemMeta() {
	}
	
	public CacheKeyStudyItemMeta(String studyItemId, String ownerId) {
		this.studyItemId = studyItemId;
		this.ownerId = ownerId;
	}
	
	public String getStudyItemId() {
		return studyItemId;
	}
	public void setStudyItemId(String studyItemId) {
		this.studyItemId = studyItemId;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result
				+ ((studyItemId == null) ? 0 : studyItemId.hashCode());
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
		CacheKeyStudyItemMeta other = (CacheKeyStudyItemMeta) obj;
		if (ownerId == null) {
			if (other.ownerId != null)
				return false;
		} else if (!ownerId.equals(other.ownerId))
			return false;
		if (studyItemId == null) {
			if (other.studyItemId != null)
				return false;
		} else if (!studyItemId.equals(other.studyItemId))
			return false;
		return true;
	}
	
	
}
