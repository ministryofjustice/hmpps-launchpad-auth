insert into public.client  ("id","authorized_grant_types", "auto_approve",  "description", "enabled", "logo_uri", "name", "registered_redirect_uris", "scopes", "secret")
values ('51133d0b-b174-4489-8f59-28794ea16129','["AUTHORIZATION_CODE", "REFRESH_TOKEN"]',true,'The Launchpad application allows you to access in-cell services',true,'','Launchpad Homepage','["http://localhost:3000/sign-in/callback", "https://launchpad-home-dev.hmpps.service.justice.gov.uk/sign-in/callback", "https://launchpad-home-staging.hmpps.service.justice.gov.uk/sign-in/callback"]','["USER_BASIC_READ", "USER_BOOKING_READ", "USER_ESTABLISHMENT_READ", "USER_CLIENTS_READ", "USER_CLIENTS_DELETE"]','$2a$10$B.eSPjBtV5NnbllRLQ3nyO0XU1GiOLq6rU5.hbnKJCjyqCwGfuGey'
       );