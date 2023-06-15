insert into public.client  ("id", "auto_approve", "description", "enabled", "logo_uri", "name", "secret")
values ('179b35cb-9fe8-46cc-b447-ab77d2687224','true','Update Test App',true,'http://localhost:8080/test','Test App','e2d6b6dd-de31-416a-a302-14e615e3b62a');

insert into public.client_authorized_grant_types("client_id", "authorized_grant_types")
values
     ('179b35cb-9fe8-46cc-b447-ab77d2687224','REFRESH_TOKEN'),
     ('179b35cb-9fe8-46cc-b447-ab77d2687224','AUTHORIZATION_CODE');

insert into public.client_scopes("client_id", "scopes")
values
    ('179b35cb-9fe8-46cc-b447-ab77d2687224','USER_ESTABLISHMENT_READ'),
    ('179b35cb-9fe8-46cc-b447-ab77d2687224','USER_BASIC_READ'),
    ('179b35cb-9fe8-46cc-b447-ab77d2687224','USER_BOOKING_READ');

insert into public.client_registered_redirect_uris("client_id","registered_redirect_uris")
values ('179b35cb-9fe8-46cc-b447-ab77d2687224','http://localhost:8080/test')