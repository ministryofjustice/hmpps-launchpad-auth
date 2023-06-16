create table public.client (
                               id uuid primary key not null,
                               authorized_grant_types jsonb not null,
                               auto_approve boolean,
                               description character varying(255) not null,
                               enabled boolean,
                               logo_uri character varying(255) not null,
                               name character varying(255) not null,
                               registered_redirect_uris jsonb not null,
                               scopes jsonb not null,
                               secret character varying(255) not null
);