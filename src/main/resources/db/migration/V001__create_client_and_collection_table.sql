create table public.client (
                               id uuid primary key not null,
                               auto_approve boolean,
                               description character varying(255) not null ,
                               enabled boolean,
                               logo_uri character varying(255) not null ,
                               name character varying(255) not null ,
                               secret character varying(255) not null
);

create table public.client_authorized_grant_types (
                                                      client_id uuid not null,
                                                      authorized_grant_types character varying(255) not null constraint client_authorized_grant_types_check
                                                          check ((authorized_grant_types)::text = ANY
                                                          ((ARRAY ['AUTHORIZATION_CODE'::character varying, 'REFRESH_TOKEN'::character varying])::text[])),
                                                      foreign key (client_id) references public.client (id)
                                                          match simple on update no action on delete no action
);

create table public.client_registered_redirect_uris (
                                                        client_id uuid not null,
                                                        registered_redirect_uris character varying(255) not null ,
                                                        foreign key (client_id) references public.client (id)
                                                            match simple on update no action on delete no action
);

create table public.client_scopes (
                                      client_id uuid not null,
                                      scopes character varying(255) not null constraint client_scopes_check
                                          check ((scopes)::text = ANY
                                          ((ARRAY ['USER_BASIC_READ'::character varying, 'USER_ESTABLISHMENT_READ'::character varying, 'USER_BOOKING_READ'::character varying])::text[])),
                                      foreign key (client_id) references public.client (id)
                                          match simple on update no action on delete no action
);