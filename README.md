# HMPPS Launchpad Auth

# Dependencies: Needs following to run the application locally.
1. Postgresql
2. Java 19
3. Intellij IDE or it can be also run from command line
4. Docker desktop


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
      DB_ENDPOINT: "localhost:5432"


To run locally from command line use following. For env variable with value XXX, get the values from dev env kubernetes secret.
AZURE_TENANT_ID=XXX AZURE_CLIENT_ID=XXX DB_ENDPOINT=localhost:5432 DB_NAME=launchpad DB_PASSWORD=password DB_USERNAME=postgres ./gradlew bootRun --args='--spring.profiles.active=dev'

To run Auth service in docker desktop in mac:
Get the kubernetes secret required and add a dev.env file in a secure folder. Update env_file in docker-compose with path to dev.env file.
Go to Launchpad Auth root directory and run command
docker-compose up

This will pull postgres image with version 15.4 and create database launchpad and run the postgresql on localhost on  port 5432.
This will also create image of launchpad auth service and run the service in localhost on port 8080.