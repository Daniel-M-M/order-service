Per non dimenticare:

Usato un comando per vincolare il repository esterno nella network del cluster usando il commando sotto:

    docker network connect "kind" "${reg_name}"

Modificato la chart di helm per avere una ConfigMap:

    apiVersion: v1
    kind: ConfigMap
    metadata:
    name: local-registry-hosting
    namespace: kube-public
    data:
    localRegistryHosting.v1: |
    host: "localhost:5000"
    help: "https://kind.sigs.k8s.io/docs/user/local-registry/"

Modificato values per avere come riferimento il registro esterno e la tag per le imagine docker generate:

    image:
    repository: localhost:5000/order_service
    # This sets the pull policy for images.
    pullPolicy: IfNotPresent
    # Overrides the image tag whose default is the chart appVersion.
    tag: "1.0.1"

Esempio implementazione per keycloak

first look at this [link](https://www.baeldung.com/keycloak-oauth2-openid-swagger)

keycloak for [cluster](https://www.keycloak.org/getting-started/getting-started-kube)
keycloak for [docker](https://www.keycloak.org/getting-started/getting-started-docker)

## 1. Il Flusso: Cosa deve fare il Frontend?

Quando un utente fa il login sul frontend (es. React, Angular, Vue):

1. Il frontend reindirizza l'utente a **Keycloak**.
2. L'utente si autentica e Keycloak restituisce al frontend un **Access Token (JWT)**.
3. Da quel momento in poi, per ogni richiesta verso il microservizio degli ordini, il frontend deve inserire l'Access Token nell'header HTTP:
   `Authorization: Bearer <IL_TUO_JWT_TOKEN>`

Il microservizio degli ordini non parlerà direttamente con Keycloak per ogni richiesta: estrarrà il JWT dall'header, ne verificherà la firma crittografica e controllerà i ruoli dell'utente.

## 2. Passo 1: Configurare il Contratto (OpenAPI / Swagger YAML)

Visto che usi il Contract-First, devi definire la sicurezza direttamente nel file di configurazione di Swagger, in modo che i generatori di codice (come `openapi-generator-maven-plugin`) creino le interfacce con le predisposizioni corrette.

Aggiungi i `securitySchemes` in fondo al file e applicali globalmente o sui singoli endpoint:

```yaml
openapi: 3.0.3
info:
  title: Order Microservice API
  version: 1.0.0

# 1. Applica la sicurezza a tutti gli endpoint (o spostalo sotto i singoli path)
security:
  - KeycloakBearerAuth: []

paths:
  /orders:
    get:
      summary: Ottieni gli ordini
      responses:
        '200':
          description: Successo

components:
  # 2. Definisci lo schema di sicurezza Bearer (JWT)
  securitySchemes:
    KeycloakBearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: Inserisci il token JWT ottenuto da Keycloak.

```

## 3. Passo 2: Configurare Spring Boot come Resource Server

Nel tuo `pom.xml`, aggiungi la dipendenza per la sicurezza OAuth2:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

```

Nel file `application.yml` del microservizio, indica a Spring Boot dove trovare la chiave pubblica di Keycloak per validare i token:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Sostituisci con il tuo host, porta e nome del Realm su Keycloak
          issuer-uri: http://localhost:8080/realms/mio-realm
          jwk-set-uri: http://localhost:8080/realms/mio-realm/protocol/openid-connect/certs

```

## 4. Passo 3: La classe di configurazione di Spring Security

Devi proteggere gli endpoint del microservizio, permettendo però il libero accesso a Swagger UI per ragioni di test.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Spesso disabilitato nei microservizi stateless
            .authorizeHttpRequests(auth -> auth
                // Permetti l'accesso pubblico a Swagger e documentazione OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Tutto il resto richiede autenticazione
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            
        return http.build();
    }
}

```

## 5. Come testare tutto da Swagger UI?

Se usi la libreria `springdoc-openapi-ui` per visualizzare l'interfaccia grafica di Swagger nel browser, noterai che in alto a destra comparirà un bottone **"Authorize"** con un lucchetto.

1. Fai una chiamata a Keycloak dal tuo frontend (o tramite Postman) per ottenere un JWT di test.
2. Clicca su **Authorize** in Swagger UI.
3. Incolla il JWT nel campo di testo.
4. Da quel momento, ogni richiesta "Try it out" fatta da Swagger includerà automaticamente l'header `Authorization: Bearer <token>`.

### Consiglio Bonus per i Ruoli

Keycloak inserisce i ruoli dentro un claim del JWT chiamato `realm_access.roles`. Spring Security di base non mappa automaticamente questo campo nei classici `GrantedAuthority` (cerca invece il claim `scope`).
Se hai bisogno di proteggere gli ordini in base ai ruoli (es. `.requestMatchers("/orders/**").hasRole("USER")`), dovrai implementare un `JwtAuthenticationConverter` customizzato per estrarre i ruoli da Keycloak.

id: 727f0600-2aeb-4770-8afa-51cba1a519a2
token: eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5ZjkyOWYwYS1jZTdlLTQ5ODMtODI5ZC1kMWYwNjJlZGM2MTkifQ.eyJleHAiOjE3ODQ5MDcwNDIsImlhdCI6MTc4NDgyMDY0MiwianRpIjoiNzI3ZjA2MDAtMmFlYi00NzcwLThhZmEtNTFjYmExYTUxOWEyIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3JlYWxtcy9mb29kbWFuYWdlciIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MS9yZWFsbXMvZm9vZG1hbmFnZXIiLCJ0eXAiOiJJbml0aWFsQWNjZXNzVG9rZW4iLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdfQ.kTAs_4hP0VNPepum8FsnnhHHI1oUCqewwVgXixh8_W_tgEnmi--HS_sPoD7qL7AhJWtttHRnh6WZEMgm7fnG1w

## **Client Credentials Keycloak**

[tutorial-medium](https://medium.com/@nsalexamy/keycloak-and-spring-boot-oauth-2-0-and-openid-connect-oidc-authentication-304e7b511d02)

Client ID: foodmanager

CLient Secret: BP9654GivSS4Ly028TutmIDKByOXlz9967ECW5QazEsEDa0O7hBEBNiFHRcfnHfsX94ddijxNuZiiS71mAWmSt