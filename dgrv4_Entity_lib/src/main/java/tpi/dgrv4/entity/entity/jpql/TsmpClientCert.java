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
@Table(name = "tsmp_client_cert")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpClientCert extends TsmpClientCertBasic{

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "client_cert_id")
	private Long clientCertId;
 

	/* constructors */
	public TsmpClientCert() {}	

	public TsmpClientCert(String clientId, String pubKey, String certFileName) {
		this.clientId = clientId;
		this.pubKey = pubKey;
		this.certFileName = certFileName;
	}

	/* methods */
	
	@Override
	public String toString() {
		return "TsmpClientCert [clientCertId=" + clientCertId + ", clientId=" + getClientId() + ", certFileName="
				+ getCertFileName() + ", fileContent=" + Arrays.toString(getFileContent()) + ", pubKey=" + getPubKey()
				+ ", certVersion=" + getCertVersion() + ", certSerialNum=" + getCertSerialNum() + ", sAlgorithmId=" 
				+ getsAlgorithmId() + ", algorithmId=" + getAlgorithmId() + ", certThumbprint=" + getCertThumbprint() 
				+ ", iuid=" + getIuid() + ", issuerName=" + getIssuerName() + ", suid=" + getSuid() + ", createAt=" + getCreateAt() + ", expiredAt="
				+ getExpiredAt() + ", keySize=" + getKeySize() + ", createDateTime=" + getCreateDateTime() + ", createUser="
				+ getCreateUser() + ", updateDateTime=" + getUpdateDateTime() + ", updateUser=" + getUpdateUser() + ", version="
				+ getVersion() + "]\n";
	}

	/* getters and setters */
	
	public Long getClientCertId() {
		return clientCertId;
	}

	public void setClientCertId(Long clientCertId) {
		this.clientCertId = clientCertId;
	}
}
