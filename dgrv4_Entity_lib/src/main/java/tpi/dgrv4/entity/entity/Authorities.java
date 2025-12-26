package tpi.dgrv4.entity.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "authorities")
@IdClass(value = AuthoritiesId.class)
public class Authorities implements Serializable{

	@Id
	@Column(name = "username")
	private String username;

	@Id
	@Column(name = "authority")
	private String authority;

	/* constructors */

	public Authorities() {}

	/* methods */

	@Override
	public String toString() {
		return "Authorities [username=" + username + ", authority=" + authority + "]\n";
	}

	/* getters and setters */

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
}
