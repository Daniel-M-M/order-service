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