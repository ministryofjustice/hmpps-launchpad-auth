# HMPPS Launchpad Auth

# Dependencies: Needs following to run the application locally.
1. Postgresql
2. Java 19
3. Intellij IDE or it can be also run from command line
4. Docker desktop


The Auth service application needs some kubernetes secrets which can be accessed from hmpps-launchpad-auth-dev namespace.
Use kubectl command to get namespace secrets.
```
Kubectl -n hmpps-launchpad-auth-dev get secrets
```

These secrets can be passed as env variables when running the application from either command line or intellij.
 
Get following secret from namespace
```
HMPPS_AUTH_CLIENT_ID: "HMPPS_AUTH_CLIENT_ID"
HMPPS_AUTH_CLIENT_SECRET: "HMPPS_AUTH_CLIENT_SECRET"
AZURE_TENANT_ID: "AZURE_TENANT_ID"
AZURE_CLIENT_ID: "AZURE_CLIENT_ID"
```

The following kubernetes variables secret is not required as postgresql running locally. Add these values:
```
DB_NAME: "launchpad"
DB_USERNAME: "postgres"
DB_PASSWORD: "password"
DB_ENDPOINT: "localhost:5432"
DB_SSL_MODE: "prefer"
```


For env variable with value XXX, get the values from dev env kubernetes secret. To run locally from command line use following.
```
AZURE_TENANT_ID=XXX AZURE_CLIENT_ID=XXX DB_ENDPOINT=localhost:5432 DB_NAME=launchpad DB_PASSWORD=password DB_USERNAME=postgres ./gradlew bootRun --args='--spring.profiles.active=dev'
```

To run Auth service in docker desktop in mac:
Get the kubernetes secret required and add a dev.env file in root directory. Do not commit dev.env file. 
Go to Launchpad Auth root directory and run command
```
docker-compose up
```

This will pull postgres image with version 15.4 and create database launchpad and run the postgresql on localhost on  port 5432.
This will also create image of launchpad auth service and run the service in localhost on port 8080.