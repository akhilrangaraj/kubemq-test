apiVersion: apps/v1
kind: Deployment
metadata:
  name: consumer-deployment
  labels:
    app: consumer
spec:
  replicas: 3
  selector:
    matchLabels:
      app: consumer
  template:
    metadata:
      labels:
        app: consumer
    spec:
      containers:
        - name: consumer
          image: {{IMAGE LOCATION}}
          command: ["java","-cp", "/app/kubemq-test.jar", "net.rangaraj.kubemq.test.Consumer", "-H", {{KUBEMQ HOST}}]