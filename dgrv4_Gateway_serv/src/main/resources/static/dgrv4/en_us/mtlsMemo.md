# mTLS Configuration Guide

## 1. Certificate Overview

TLS certificates are used to establish encrypted communication channels and verify identities.

### Common Certificate Types

| Type             | Purpose                        | Example                 |
|------------------|--------------------------------|--------------------------|
| Server Certificate | Verifies server identity       | HTTPS, API Gateway       |
| Client Certificate | Verifies client/user identity | mTLS, VPN                |
| Intermediate CA   | Bridges Root CA and certificate | Part of trust chain      |
| Root CA           | Pre-trusted by systems/browsers | DigiCert, Let's Encrypt  |
| Self-signed Cert  | Self-generated trust           | Used in dev/test env     |

### Certificate Formats

| Extension    | Description                        |
|--------------|------------------------------------|
| .crt/.pem    | Base64-encoded certificate         |
| .key         | Private key                        |
| .p12/.pfx    | Certificate + key (optional password) |
| .csr         | Certificate Signing Request        |

---

## 2. Configuration Steps

### 1. Enable Certificate Verification

Add the following to `application.properties`:

```
mtls.certificate.verification.enabled=true
```

> Restart `digiRunner` after changes take effect.

---

### 2. Configure Client Certificate (via AC Console)

Path: AC → Client Certificate Management → Create → Enable

| Field         | Description                                      |
|---------------|--------------------------------------------------|
| Host & Port   | Auto-uses this certificate when matching         |
| Root CA       | CA cert provided by server                      |
| Client Cert   | Client certificate (.crt/.pem)                  |
| Client Key    | Matching private key (.key/.pem)                |
| Key Password  | If key is encrypted, enter the password (will be stored encrypted) |

---

## 3. mTLS Certificate Preparation Process

1. **Obtain from Server:**
    - Server's CA cert (Root / Intermediate)
    - Client cert requirement (CSR or full cert)

2. **Obtain/Create Client Certificate:**
    - Generate CSR and have server sign it
    - Or get the cert + key directly from server

3. **Chain Certificates (if needed):**
```
-----BEGIN CERTIFICATE-----
(Client Certificate)
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
(Intermediate CA)
-----END CERTIFICATE-----
```

---

## 4. Enable mTLS for API

In the API registration screen, check "Enable mTLS" — otherwise, API requests will be rejected.

---

## 5. Common Errors & Troubleshooting

| Error Message                          | Cause                        | Solution                             |
|----------------------------------------|-------------------------------|---------------------------------------|
| unable to verify the first certificate | Missing Root/Intermediate CA | Provide full certificate chain        |
| self signed certificate                | Using untrusted self-signed cert | Trust it manually or use a trusted CA |
| certificate expired                    | Expired cert                  | Reissue certificate                   |
| hostname mismatch                      | CN/SAN doesn't match hostname | Correct hostname or cert             |

---

## 6. Common Questions

**Q1: Can I use a Server Certificate as a Client Certificate?**  
A: Normally no. Server certificates should only be used for servers. Exceptions:

- Server did not enable mTLS
- Dev/test environment without strict validation (not recommended)

**Q2: Can I use only an Intermediate CA as the Client Certificate?**  
A: No. You must include the actual end-user client certificate. Recommended:

- Combine Client cert + Intermediate cert into a single `.crt` file