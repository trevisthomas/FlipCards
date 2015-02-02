package org.ttdc.flipcards.server;

import java.io.Serializable;

public class CacheKeyPageination implements Serializable{
	private static final long serialVersionUID = -1423246829736745185L;
	private String owner;
	private long page;

	public CacheKeyPageination() {
	
	}
	
	public CacheKeyPageination(String owner, long page) {
		this.owner = owner;
		this.page = page;
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public long getPage() {
		return page;
	}
	public void setPage(long page) {
		this.page = page;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + (int) (page ^ (page >>> 32));
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
		CacheKeyPageination other = (CacheKeyPageination) obj;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (page != other.page)
			return false;
		return true;
	}
	
	
}
