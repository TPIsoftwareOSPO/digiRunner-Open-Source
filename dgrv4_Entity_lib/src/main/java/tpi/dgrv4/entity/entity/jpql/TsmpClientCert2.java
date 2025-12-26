package tpi.dgrv4.entity.entity.jpql;

import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_client_cert2")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpClientCert2 extends TsmpClientCertBasic{

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "client_cert2_id")
	private Long clientCert2Id;
 

	/* constructors */
	
	public TsmpClientCert2() {
	}

	/* methods */
	
	@Override
	public String toString() {
		return "TsmpClientCert [clientCert2Id=" + clientCert2Id + ", clientId=" + getClientId() + ", certFileName="
				+ getCertFileName() + ", fileContent=" + Arrays.toString(getFileContent()) + ", pubKey=" + getPubKey()
				+ ", certVersion=" + getCertVersion() + ", certSerialNum=" + getCertSerialNum() + ", sAlgorithmId=" 
				+ getsAlgorithmId() + ", algorithmId=" + getAlgorithmId() + ", certThumbprint=" + getCertThumbprint() 
				+ ", iuid=" + getIuid() + ", issuerName=" + getIssuerName() + ", suid=" + getSuid() + ", createAt=" + getCreateAt() + ", expiredAt="
				+ getExpiredAt() + ", keySize=" + getKeySize() + ", createDateTime=" + getCreateDateTime() + ", createUser="
				+ getCreateUser() + ", updateDateTime=" + getUpdateDateTime() + ", updateUser=" + getUpdateUser() + ", version="
				+ getVersion() + "]\n";
	}

	/* getters and setters */
	
	public Long getClientCert2Id() {
		return clientCert2Id;
	}

	public void setClientCertId(Long clientCert2Id) {
		this.clientCert2Id = clientCert2Id;
	}
}
