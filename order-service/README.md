# ORDER-SERVICE (core app)

Questo servizio ha come obiettivo esser un servizio di base per lo sviluppo di
microservice. Lo stack include db postgres, backend in Spring-boot. In aggiunta
per la creazione del ambiente di sviluppo usiamo un cluster di kubernetes usando
[kind](https://kind.sigs.k8s.io/) e un repository nella stessa rete del cluster.
La parte di authentication e authorization è gestita da un container Keycloak 
fuori cluster associato a un db postgres. Il servizio è gestito con [helm chart](https://helm.sh/it/)

## Creazione del Cluster

Per criare il cluster dobbiamo usare un file come questo descrito sotto, che ha
la configurazione per il registry. Il servizio order-service fa uso di 'actuator' per il livness
e readness di kubernets.

    kind: Cluster
    apiVersion: kind.x-k8s.io/v1alpha4
    containerdConfigPatches:
    - |-
      [plugins."io.containerd.grpc.v1.cri".registry.mirrors."localhost:5000"]
      endpoint = ["http://test-register:5000"]
      nodes:
      - role: control-plane
        extraPortMappings:
          - containerPort: 80
            hostPort: 81
            protocol: TCP
      - role: worker
        extraMounts:
          - hostPath: /home/dmm/learn_k8s/DevOps_Directive/kind-pv
            containerPath: /some/path/in/container
      - role: worker
        extraMounts:
          - hostPath: /home/dmm/learn_k8s/DevOps_Directive/kind-pv
            containerPath: /some/path/in/container

Dopo aver salvato il file usiamo il comando di kind per creare il cluster:

```bash
kind create cluster --name my-cluster --config kind-example-config.yaml
```

## Creating a Registry

Usando un esempio dal sito di Kind:

```bash
reg_name='kind-registry'
reg_port='5001'
if [ "$(docker inspect -f '{{.State.Running}}' "${reg_name}" 2>/dev/null || true)" != 'true' ]; then
  docker run \
    -d --restart=always -p "127.0.0.1:${reg_port}:5000" --network bridge --name "${reg_name}" \
    
```

Usato un comando per vincolare il repository esterno nella network del cluster usando il commando sotto:
```bash
docker network connect "kind" "${reg_name}"
```

## Helm Configuration

Crea una chart di helm e aggiunge una configMap per il registry local appena creato:

    apiVersion: v1
    kind: ConfigMap
    metadata:
    name: local-registry-hosting
    namespace: kube-public
    data:
      localRegistryHosting.v1: |
        host: "localhost:5000"
        help: "https://kind.sigs.k8s.io/docs/user/local-registry/"

Modificato values di helm per fare riferimento il registro esterno invece di usare il
registro di docker locale, e la tag per le imagine docker generate da maven deploy:

    image:
    repository: localhost:5000/order_service
    # This sets the pull policy for images.
    pullPolicy: IfNotPresent
    # Overrides the image tag whose default is the chart appVersion.
    tag: "1.0.1"

## Configurazione Keycloak

Creazione di un container Keycloak da docker-compose presente nel repo e creazione di
un container postgres per il keycloak service:

    db per keycloak and keycloak container
    
    keycloak-postgresql:
      image: postgres:16.8
      restart: always
      ports:
        - 5435:5432
      volumes:
        - ./db_keycloak :/var/lib/postgresql/data
      environment:
        POSTGRES_DB: keycloak
        POSTGRES_USER: keycloak
        POSTGRES_PASSWORD: password
        POSTGRES_HOST_AUTH_METHOD: trust
      healthcheck:
        test: [ "CMD-SHELL", "pg_isready" ]
        interval: 30s
        timeout: 5s
        retries: 5
      logging:
        driver: "json-file"
        options:
          max-size: "200k"
          max-file: "5"

    keycloak:
      image: quay.io/keycloak/keycloak:26.7.0
      container_name: keycloak
      command: start-dev
      restart: always
      ports:
        - 8081:8080
      environment:
        KC_BOOTSTRAP_ADMIN_USERNAME: admin
        KC_BOOTSTRAP_ADMIN_PASSWORD: admin
        KC_SPI_ADMIN_REALM: master
        KEYCLOAK_HTTP_RELATIVE_PATH: /
    
        KC_DB: postgres
        KC_DB_URL: jdbc:postgresql://keycloak-postgresql:5432/keycloak
        KC_DB_USERNAME: keycloak
        KC_DB_PASSWORD: password
      depends_on:
        keycloak-postgresql:
          condition: service_healthy

Per far si che il nostro integration test funzioni abbiamo bisogno di un file di configurazione
di keycloak per installare durante la fase di creazione del conainer keycloak di test.
Quindi usando il commando Docker possiamo aver queto file:

```bash
docker exec -it containerId /opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --realm myRealm --users realm_file
```

Adesso puoi usare il file per configurare il container di test di keycloak, dentro questo
repo ho un esempio di file strato da un container keycloak di test 
[foodmanager-realm](/src/test/resources/IT/keycloak/foodmanager-realm.json). 

## Risoluzione dei problemi comuni

### Errore di connessione a Keycloak nel Pod (Connection refused)

Se riscontri un errore `Connection refused` durante l'avvio del pod Kubernetes (Kind), è probabilmente perché l'applicazione sta cercando di connettersi a `localhost:8081`. Nel contesto di un pod, `localhost` si riferisce al pod stesso, mentre Keycloak è in esecuzione all'esterno del cluster.

**Soluzione:**
Devi aggiornare il segreto `order-service-secrets` o le variabili di ambiente `OAUTH2_ISSUER_URI` e `OAUTH2_JWT_ISSUER_URI` per puntare all'indirizzo IP dell'host.

Se usi **Kind su Linux**, puoi solitamente usare l'IP del bridge docker (es. `172.17.0.1`):
```bash
OAUTH2_ISSUER_URI=http://172.17.0.1:8081/realms/foodmanager
```

Se usi **Docker Desktop**, puoi usare:
```bash
OAUTH2_ISSUER_URI=http://host.docker.internal:8081/realms/foodmanager
```

Assicurati che il valore nel segreto Kubernetes `order-service-secrets` (chiave `keycloak_issuer_uri`) sia aggiornato correttamente.

**Esempio implementazione per keycloak**

- first look at this [link](https://www.baeldung.com/keycloak-oauth2-openid-swagger)
- keycloak for [cluster](https://www.keycloak.org/getting-started/getting-started-kube)
- keycloak for [docker](https://www.keycloak.org/getting-started/getting-started-docker)

**Client Credentials Keycloak**

- [tutorial-medium](https://medium.com/@nsalexamy/keycloak-and-spring-boot-oauth-2-0-and-openid-connect-oidc-authentication-304e7b511d02)
- Link to [Integration-test-keycloak](https://www.baeldung.com/spring-boot-keycloak-integration-testing)

## ORDER SERVICE VALEUS CONFIGURATION

| Key | Value | description |
|-----|-----|-----|
| image.repository | localhost:5000/order_service | Point to local repository container |
| image.tag | "1.0.4" | Tag for image on registry |
| livenessProbe.httpGet.path | /actuator/health/liveness | use actuator |
| readinessProbe.httpGet.path | /actuator/health/readiness | use actuator |
| volumeMounts.mountPath | /config/openapi | The same of application.properties.yaml spring.web.resources.static-locations=file:**/config/openapi/**,classpath:/static/ |
| config.keycloak.external.redirectUri | http://\<hostname>:\<port>\/swagger-ui/oauth2-redirect.html | Redirect to swagger the login result | 
| config.keycloak.external.issuer | http://\<hostname>:8081/realms/<my-healm> | Used by spring to get keycloak public key and validate |
| config.keycloak.external.authUrl | http://\<hostname>:8081/realms/<my-healm>/protocol/openid-connect/auth | Used to authenticate on keycloak |
| config.keycloak.external.tokenUrl | http://\<hostname>:8081/realms/<my-healm>/protocol/openid-connect/token | Used to retrieve the token |
| config.swagger.port | 3030 | this port is changeable and rifer to a forward swagger-ui of the svc |

## SPLUNK CONFIGURATION

Nel nostro caso abbiamo dovuto installare Splunk OpenTelemetry Collector in un namespace separato dal servizio usando un values.yaml file apposta per la nostra situazione.

    clusterName: kind-ci-cd-learn
    
    logsCollection:
        containers:
            enabled: true
            containerRuntime: containerd
    
    secret:
        create: false
        name: splunk-hec-token
    
    splunkPlatform:
        endpoint: https://172.18.0.1:8088/services/collector
        index: orders
        insecureSkipVerify: true
        source: kubernetes
        token: ""
