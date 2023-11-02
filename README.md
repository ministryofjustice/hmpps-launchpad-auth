# HMPPS Launchpad Auth

# Dependencies: Needs following to run the application locally.
1. Postgresql
2. Java 19
3. Intellij IDE or it can be also run from command line.

# Use Docker desktop to run the postgresql image
docker pull postgres:15.4
run the docker with setting port = 5432
set environment variable POSTGRES_PASSWORD = password

or
docker run postgres:15.4 -p 5432

After running the postgresql use any sql client tool to create a database launchpad in docker.

After adding database launchpad run the Launchpad Auth service either using intellij or from command line.

The Auth service application needs some kubernetes secret which can be accessed from hmpps-launchpad-auth-dev namespace.
Use Kubectl -n hmpps-launchpad-auth-dev get secrets. These secrets can be passed as env variables when running the application from either command line or intellij. Do not add these secrets in env files
as there is chance of committing it to remote repository by mistake. 
Get following secret from namespace
      HMPPS_AUTH_CLIENT_ID: "HMPPS_AUTH_CLIENT_ID"
      HMPPS_AUTH_CLIENT_SECRET: "HMPPS_AUTH_CLIENT_SECRET"
      AZURE_TENANT_ID: "AZURE_TENANT_ID"
      AZURE_CLIENT_ID: "AZURE_CLIENT_ID"
For following add and kubernetes secret is not required as postgresql running locally:
      DB_NAME: "launchpad"
      DB_USERNAME: "postgres"
      DB_PASSWORD: "password"
      DB_ENDPOINT: "localhost"


To run locally from command line use following. For env variable with value XXX, get the values from dev env kubernetes secret.
AZURE_TENANT_ID=XXX AZURE_CLIENT_ID=XXX DB_ENDPOINT=localhost:5432 DB_NAME=launchpad DB_PASSWORD=password DB_USERNAME=postgres ./gradlew bootRun --args='--spring.profiles.active=dev'

To run Auth service in docker desktop in mac:
docker build -t launchpadauth:latest .
docker run  -e 'SPRING_PROFILES_ACTIVE=dev' -e 'AZURE_TENANT_ID=XXX' -e 'AZURE_CLIENT_ID=XXX'  -e 'DB_ENDPOINT=docker.for.mac.localhost:5432' -e 'DB_NAME=launchpad' -e 'DB_USERNAME=postgres' -e 'DB_PASSWORD=password' launchpadauth:latest -p 8080:80
