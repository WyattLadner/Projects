This is a Simple and Secure, open-source application that enables people to host their own password manager on their local network. This was developed as a student project at the University of South Alabama in CSC 440.


## Below is how to setup tls for secure communication with your server
# 1. Server PKCS12 keystore with self-signed cert
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 3650 \`
  -keystore server.keystore.p12 -storetype PKCS12 -storepass changeit -keypass changeit `
  -dname "CN=localhost, OU=IT, O=MyOrg, L=City, S=State, C=US"

# 2. Export the server certificate
keytool -export -alias server -keystore server.keystore.p12 -storepass changeit -rfc -file server.crt

# 3. Create client truststore and import server certificate
keytool -import -alias server -file server.crt -keystore client.truststore.p12 -storetype PKCS12 -storepass changeit -noprompt

## These must be in a folder WITH the exe, clientkey with client and serverkey with server.

