* Veritabanı hazırlığı
	- db bilgileri
		url: localhost:5432/postgres
		user: postgres
		pass: 12345678
	
	- tablo
		CREATE TABLE public.authorities (
			id serial4 NOT NULL,
			username varchar(45) NOT NULL,
			authority varchar(45) NOT NULL,
			CONSTRAINT authorities_pkey PRIMARY KEY (id)
		);
	
* SpringBoot Hazırlığı
	- application.properties
		spring.application.name=vault-example

		#db config
		spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
		spring.datasource.username=postgres
		spring.datasource.password=12345678
		spring.datasource.driver-class-name=org.postgresql.Driver

		# jpa config
		spring.jpa.hibernate.ddl-auto=validate
		spring.jpa.show-sql=true
		spring.jpa.properties.hibernate.format_sql=true
		spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

		# server config
		server.port=8080
		
	- build.gradle
		dependencies {
			implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
			implementation 'org.springframework.boot:spring-boot-starter-web'
			implementation 'org.springframework:spring-web:6.2.11'
			implementation 'org.postgresql:postgresql:42.7.3'
			compileOnly 'org.projectlombok:lombok'
			developmentOnly 'org.springframework.boot:spring-boot-devtools'
			annotationProcessor 'org.projectlombok:lombok'
		}

	- entity
		@Entity
		@Table(name = "authorities", schema = "public")
		@Data
		public class Authorites {
			@Id
			private Integer id;
			private String username;
			private String authority;
		}
	
	- repository
		@Repository
		public interface UserRepository extends JpaRepository<Authorites, Integer> {}
		
	- controller
		@RequiredArgsConstructor
		@RestController
		@RequestMapping("user")
		public class UserController {
			private final UserRepository userRepository;
			@GetMapping("/list")
			public List<Authorites> getInfo(){
				return userRepository.findAll();
			}
		}
		
	- örnek istek : http://localhost:8080/user/list
		Çıktı : [{"id":1,"username":"huseyin","authority":"admin"}]

1. Giriş
	1.1) Vault Bilgiler
		- Hassas bilgilerin güvende tutulması, saklanması ve erişimlerinin kontrol edilmesi
		- Kısaca secret management denilebilir.
		- Vault'a yazılan secretler encrypte edilerek saklanır.

	1.2) Hassas bilgiler neler olabilir?
		- Token, Şifre, Sertifika, Key
		
	1.3) Hassas bilgilere kimler erişmek isteyebilir?
		- Kişi, Uygulama, Sunucu

	1.4) Static Secret ve Dynamic Secret nedir?
		- Static secretler silinmez, sabittir
		- Dynamic secretler ihtiyaç duyulduğunda üretilir, yaşam süresi vardır

	1.5) Vault İşlemleri Nasıl Gerçekleştirilir
		- UI, Terminal CLI ile veya HTTP API ile yapılabilmektedir

	1.6) Windows kurulum
		- https://developer.hashicorp.com/vault/install adresine git
		- Windows sekmesini bul
		- AMD64 seçeneğini indir
		- D:/programfiles içerisine zipi çıkar. Dosya konumu : D:/programfiles/vault.exe
		- vault kontrolu
			# vault version
		- Açılan Vault sunucusuna başka CLI ile erişebilmek için ortam değişkenlerine set işlemi yapılır
			# set VAULT_ADDR=http://127.0.0.1:8200
		- Adres çubuğuna http://127.0.0.1:8200 yazılarak erişilebilir

2. Sunucu Tipleri
	- 3 Tip sunucu bulunur
		* Dev mode
			In memory storage
			ayağa kaldırma - otomatik
			unseal - otomatik
			root token oluşturulması - otomatik
		* Self-Managed
			Ayarlanabilir backend storage
			ayağa kaldırma - manuel
			unseal - manuel
			initial root token seal içerisinde bulunur
		* HCP Dedicated (HashiCorp)
			Entegre bir backend storage
			ayağa kaldırma - otomatik
			unseal - otomatik
			root token bulunmaz 

	2.1) Dev Mode
		- Vault sunucusunu bu modda çalıştırınca önceden yapılandırılmış ayarlar ile açılır.
		- Local ortamlar için tercih edilir, prod ortamda tercih edilmez
		- Veriler inmemory saklanır
		- dev mode'da admin yetkisiyle işlem yapılır. Diğer modlarda authentication gereklidir
		
		* Sunucunun çalıştırılması (yeni bir terminal açılma ihtiyacı olur)
			# vault server -dev
		* Sunucunun backgroundta çalıştırılması
			# vault server -dev > server.log 2>&1 &
		* Sunucu arayüz yapılandırması
			# export VAULT_ADDR='http://127.0.0.1:8200'
		* Root token verilmesi
			# export VAULT_DEV_ROOT_TOKEN_ID="s.XmpNPoi9sRhYtdKHaQhkHP6x"
		* Vault CLI'nin server ile iletişimde olduğunun kontrol edilmesi
			# vault status
				Initialized    true
				Sealed         false
		* Sunucunun sonlandırılması
			- CTRL+C ile sonlanır
			# pkill vault
			# unset VAULT_ADDR VAULT_CACERT

	2.2) Prod Ortam Senaryoları
		- Yapılandırma ihtiyacı bulunur
		- Aynı dizin içerisine "vault.hcl" isimli bir dosya oluştur, içerisine
			Örnek 1:
				ui = true

				storage "file" {
				  path = "/opt/vault/data"
				}

				listener "tcp" {
				  address     = "0.0.0.0:8200"
				  tls_disable = 1
				}

				api_addr = "http://127.0.0.1:8200"
				cluster_addr = "http://127.0.0.1:8201"	
			
			Örnek 2: 
				// storage belirlenmesi
				storage "consul" {
				 address = "127.0.0.1:8500"
				 path = "vault/"
				}
				
				// arayüz enable
				ui = true

				// HTTP listener
				listener "tcp" {
				  address = "0.0.0.0:8200"
				  tls_disable = 1
				}
				
				// HTTPS listener
				listener "tcp" {
				  address       = "0.0.0.0:8200"
				  tls_cert_file = "/opt/vault/ssl/xxxxx.crt"
				  tls_key_file  = "/opt/vault/ssl/xxxxx.key"
				  tls_client_ca_file = "/home/arif/xxxxx.ca-bundle"
				  tls_require_and_verify_client_cert= "false"
				}			

		- sunucuyu config ile başlatmak için
			# vault server -config=vault.hcl
				
		- başka bir terminal ile bağlantı yapabilmek için
			# set VAULT_ADDR=http://127.0.0.1:8200
		
		- sunucu başlatılması
			# vault operator init
			
			Çıktı :
			Unseal Key 1: vRJYWwgGOLynondmr3P0uxvUy+XX3p75Z/JfqmTUHqjw
			Unseal Key 2: 3c027f95VCqreK80vE+wJ1EcYpu7zHkSscZRGUysGT4i
			Unseal Key 3: rSxI7xX5u6xnROEMUeKuBoRhmV7DslsJAxJqXzJVLIGI
			Unseal Key 4: HEgNS6YeVDSoHQQo2kiC+wyuyJOTYKid23n7uzJAUkI4
			Unseal Key 5: ywQrXXbdEf6aE9fPoRsZqHUIbhrtmcc7lxJ85aPURMSK

			Initial Root Token: hvs.whwFn1qrMrdY1diDEXWbyHs9		

		- unseal işlemi (en az 3 tane key girilmeli)
			# vault operator unseal
				1. seal key girilebilir!
			# vault operator unseal
				2. seal key girilebilir!
			# vault operator unseal
				3. seal key girilebilir!
			# vault status
				Çıktı : Sealed = false olduğu görülebilir.

		- Giriş yapmak için
			# vault login hvs.whwFn1qrMrdY1diDEXWbyHs9
				Çıktı : Success! You are now authenticated.

		- Secret engine olarak KV seçimi
			# vault secrets enable -path=secret kv-v2
				Çıktı : Success! Enabled the kv-v2 secrets engine at: secret/
				
			# vault secrets list
				Çıktı : 
				Path          Type         Accessor              Description
				----          ----         --------              -----------
				cubbyhole/    cubbyhole    cubbyhole_d09310ad    per-token private secret storage
				identity/     identity     identity_aeb06353     identity store
				secret/       kv           kv_082f0ae5           n/a
				sys/          system       system_0ed3f35f       system endpoints used for control, policy and debugging			

		2.2.1) Token ile

			- Örnek bir secret oluşturma
				# vault kv put secret/dbbilgileri db.username=myuser db.password=mypassword	
			
			- oluşturulan örnek secreta ulaşmak
				# vault kv get secret/dbbilgileri
					======= Data =======
					Key            Value
					---            -----
					db.password    mypassword
					db.username    myuser

			- oluşturulan örnek secreta ulaşmak (json formatında)
				# vault kv get -format=json secret/dbbilgileri
					{
					  "lease_duration": 0,
					  "renewable": false,
					  "data": {
						"data": {
						  "db.password": "mypassword",
						  "db.username": "myuser"
						}
					  }
					}
					
			- dbbilgileri için policy oluşturulması 
				- "dbbilgileri-policy.hcl" isimli bir dosya oluşturulur, içerisine
					path "secret/data/dbbilgileri" {
					  capabilities = ["read"]
					}
					
				- policy eklenmesi
					# vault policy write dbbilgileri dbbilgileri-policy.hcl
						Çıktı : Success! Uploaded policy: dbbilgileri
				
				- policy içeriğinin görüntülenmesi
					# vault policy read dbbilgileri
				
			- dbbilgileri policy için bir token oluşturulması
				# vault token create -policy="dbbilgileri" -ttl=24h
					Çıktı :
					Key                  Value
					---                  -----
					token                hvs.CAESILvNvqVTNG-4qd8SqpQDb83oTKTF8u3eKnqzMa6DH77UGh4KHGh2cy5ERFlhUmxUVzliZ3JhVldwYVY2UlFXVmo
					token_accessor       NMCs5gDFNzJoZhPhIeAy3asQ
					token_duration       24h
					token_renewable      true
					token_policies       ["dbbilgileri" "default"]
					identity_policies    []
					policies             ["dbbilgileri" "default"]				
				
			- audit loglarının tutulması ????
				# vault audit enable file file_path=/var/log/vault_audit.log
				
			- build.gradle
				implementation 'org.springframework.cloud:spring-cloud-starter-vault-config:4.3.0'

			- application.properties
				spring.cloud.vault.uri=http://127.0.0.1:8200
				spring.cloud.vault.token=hvs.CAESILvNvqVTNG-4qd8SqpQDb83oTKTF8u3eKnqzMa6DH77UGh4KHGh2cy5ERFlhUmxUVzliZ3JhVldwYVY2UlFXVmo
				spring.cloud.vault.kv.enabled=true
				spring.cloud.vault.kv.backend=secret
				spring.cloud.vault.kv.default-context=springboot
				spring.cloud.vault.kv.profile-separator="-"
				spring.datasource.username: ${db.username}
				spring.datasource.password: ${db.password}
		
		2.2.2) AppRole ile
			- AppRole aktifleştirilmesi
				# vault auth enable approle
					Çıktı : Success! Enabled approle auth method at: approle/
			
			- aktif auth görüntülenmesi
				# vault auth list
					Çıktı :
					Path        Type       Accessor                 Description                Version
					----        ----       --------                 -----------                -------
					approle/    approle    auth_approle_2e47a4ef    n/a                        n/a
					token/      token      auth_token_2f39f7f8      token based credentials    n/a			
			
			- uygulama için policy oluşturulması 
				- "myapp-policy.hcl" isimli bir dosya oluşturulur, içerisine
					path "secret/data/vault-example" {
					  capabilities = ["read"]
					}

				- policy eklenmesi
					# vault policy write myapp-policy myapp-policy.hcl
						Çıktı : Success! Uploaded policy: myapp-policy
				
				- policy içeriğinin görüntülenmesi
					# vault policy read myapp-policy			
				
			- AppRole oluşturulması
				- Login sonrası otomatik token üretilecektir
				# vault write auth/approle/role/myapp-role token_policies="myapp-policy" token_ttl=1h token_max_ttl=4h
					Çıktı : Success! Data written to: auth/approle/role/myapp-role
					Rol ismi : myapp-role
					Policy ismi : myapp-policy
				
			- RoleID elde edilmesi (public bir değerdir ama repoda tutulmamalı)
				# vault read auth/approle/role/myapp-role/role-id
					Çıktı :
					Key        Value
					---        -----
					role_id    5b16d789-335f-e845-1e95-a93ca7cd019b					
			
			- SecretID elde edilmesi (Çok gizli bir değerdir)
				# vault write -f auth/approle/role/myapp-role/secret-id
					Çıktı :
					Key                   Value
					---                   -----
					secret_id             42f66e7e-6843-6e06-19ab-adfeeef89a52
					secret_id_accessor    5d5df7e3-f6a8-dd33-2df2-7b0f98161cc8
					secret_id_num_uses    0
					secret_id_ttl         0s					
				
			- SecretID değerinin tek kullanımlık olması
				# vault write auth/approle/role/myapp-role secret_id_num_uses=1
			
			- SecretID için zaman aşımı
				# vault write auth/approle/role/myapp-role secret_id_ttl=10m
				
			- Secret oluşturulması
				# vault kv put secret/vault-example db.username=myuser db.password=mypassword
			
			- Secreta ekleme yapılması
				Put işlemi geçmiş tüm secretlari siler ama Patch işlemi yaptığımızda ekleme yapar
				# vault kv patch secret/vault-example db.url=jdbc:postgresql://localhost:5432/postgres
				
			- build.gradle
				implementation 'org.springframework.cloud:spring-cloud-starter-vault-config:4.3.0'

			- application.properties
				# girilen secret bilgisinin loglarda dump edilmemesi için (management.endpoint.env.enabled=false)
			
				spring.application.name=vault-example
				# server config
				server.port=8080

				#db config
				spring.datasource.url=${db.url}
				spring.datasource.username= ${db.username}
				spring.datasource.password= ${db.password}
				spring.datasource.driver-class-name=org.postgresql.Driver

				# jpa config
				spring.jpa.hibernate.ddl-auto=validate
				spring.jpa.show-sql=true
				spring.jpa.properties.hibernate.format_sql=true
				spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

				# vault config
				spring.cloud.vault.uri=http://127.0.0.1:8200
				spring.cloud.vault.authentication=APPROLE
				spring.cloud.vault.approle.role-id=${VAULT_ROLE_ID}
				spring.cloud.vault.approle.secret-id=${VAULT_SECRET_ID}
				spring.cloud.vault.kv.enabled=true
				spring.cloud.vault.kv.backend=secret
				spring.cloud.vault.kv.default-context=vault-example
				spring.config.import=vault://
				
			- ortam değişkenlerine eklenmesi
				-DVAULT_ROLE_ID=697fc1c9-27bb-f283-d544-102ea600612c -DVAULT_SECRET_ID=86f68ad7-b825-f730-8aba-8d81779275b2
				
		2.2.3) userpass ile
			- Genellikle devops ekiplerinin erişim yetkileri için tercih edilir
			- Vault içerisinde kullanıcı adı ve password oluşturulur
			- Şifre saklama problemi mevcut
			- Yazmaktan sıkıldım örneğini yapmayacağım

3. Secretlar
	3.1) Secret oluşturmak
		# vault kv put secret/db username=huseyin
			path: secret/db
			key: username
			value: huseyin
		
		# vault kv put secret/db username=huseyin password=123

	3.2) Secret okunması
		# vault kv get secret/db
			tüm secretlar
		# vault kv get -field=username secret/db
			sadece bir secret değeri
			
	3.3) Secret silinmesi
		# vault kv delete secret/db

	3.4) Secret engine enable edilmesi
		# vault secrets enable -path=kv kv

	3.5) Enable edilmiş engine listesi
		# vault secrets list
		
4. Authentication 
	- Aktif olan erişim yöntemini görmek için
		# vault auth list

	4.1) Token
		- Token authentication metodu default olarak enable gelir, disable edilemez
		- Her tokenin bir parent tokeni olur, parent iptal edilirse sub tokenler da iptal edilir.	
		- Default yöntemdir
		- Kurulum esnasında bu bilgiyi vault bize verir
		- Token tipleri
			* Periodic : Belirli sürede revoke olur
			* Service Token With Limit : Kullanım limiti aşılınca revoke olur
			* Child Token : Ebeveny tokenden üretilir. Ebeveyn revoke olursa o da revoke olur.
			* Orphan Token : Child token ile aynı, ebeveyn revoke olursa bizim tokenimiz olmaz.
			* CIDR-Bound Token : Token belirli bir host veya network için kullanılır

		4.1.1) Token oluşturma
			# vault token create
		4.1.2) Token Hakkında bilgi
			# vault token lookup hvs.aaaaaAOwN4rS56xxxxx
		4.1.3) Token yetkileri
			# vault token capabilities hvs.aaaaaAOwN4rS56xxxxx
		4.1.4) Token yenileme
			# vault token renew hvs.aaaaaAOwN4rS56xxxxx
		4.1.5) Token silme
			# vault token revoke hvs.aaaaaAOwN4rS56xxxxx
		4.1.6) Github yöntemiyle auth
			# vault auth enable github
		4.1.7) Github içerisinde hashicorp organizasyonunda yer alan kullanıcılar	
			# vault write auth/github/config organization=hashicorp
		4.1.8) Takım1 kullanıcılarına default ve policy1 policy atanması
			# vault write auth/github/map/teams/takım1 value=default,policy1
		4.1.9) Auth metodlarının listesi
			# vault auth list

	4.2) Auth
		- Aktifleştirilmesi
			# vault auth enable userpass
			# vault auth enable -path=webuser -description"userpass" userpass
		- Belirli bir geçerli süre tanımlama
			# vault auth tune -default-lease-ttl=24h userpass
		- Kullanıcı eklenmesi
			# vault write auth/userpass/users/admin password="12345678" policies=default
		- Kullanıcıları listeleme
			# vault list auth/userpass/users
			# vault readauth/userpass/users		
	4.3) AppRole 
		- Uygulamalar ve sunucular için kullanılan yöntem
		- Aktifleştirilmesi
			# vault auth enable approle
		- Kullanıcı eklenmesi
			# vault write auth/approle/role/edenetisweb policy=root token_ttl=20m
		- RoleID ve SecretID bilgilerinin elde edilmesi
			# vault read auth/approle/role/jenkins/role-id
			# vault write -f auth/approle/role/jenkins/secret-id
		- Login
			# vault write auth/approle/login role_id=$ROLEID secret_id=$SECRETID		

5. Policy
	- default ve root policy'leri standart tanımlanır ve silinemez
	- Policyler; token, grup, kişilere entegre edilmelidir

	5.1) Policylerin listelenmesi
		# vault policy list 
	5.2) Policy içeriğinin görüntülenmesi
		# vault policy read
	* ÖRNEK
		# vault secrets enable -path=secret/ kv-v2  			// kv-v2 isminde secret engine create edildi
		# vault token create -policy=my-policy					// policynin atanacağı bir token oluşturuldu
		# vault login s.X6gvFko7chPilgV0lpWXsdeu				// login işlemi
		# vault kv put secret/creds password="my-long-password" // secret oluşturulması
	
	5.3) Policy oluşturulması
		# vault policy write my-policy /tmp/policy.hcl	

6. SpringBoot	
	6.1) Secretlarin hazırlanması
		vault kv put secret/springboot-app \
		  db.url=jdbc:postgresql://localhost:5432/appdb \
		  db.username=appuser \
		  db.password=postgres123


	6.2) Bağımlılık
		<dependency>
		  <groupId>org.springframework.cloud</groupId>
		  <artifactId>spring-cloud-starter-vault-config</artifactId>
		</dependency>
		
	6.3) Configuration
		spring.cloud.vault.uri=http://localhost:8200
		spring.cloud.vault.token=root
		spring.cloud.vault.kv.enabled=true
		spring.cloud.vault.kv.backend=secret
		spring.cloud.vault.kv.application-name=springboot-app

		spring.datasource.url=${db.url}
		spring.datasource.username=${db.username}
		spring.datasource.password=${db.password}

7. Kaynaklar
	chatgpt
	https://developer.hashicorp.com/vault/docs/concepts/dev-server
	https://murataydogar86.medium.com/hashicorps-vault-842a24d21346
	https://medium.com/trendyol-tech/vault-101-e053f4108fc3
	https://arifkiziltepe.medium.com/vault-notlar%C4%B1m-568767308f6d
