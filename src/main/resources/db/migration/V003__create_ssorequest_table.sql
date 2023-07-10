create table public.sso_request (
                                    created_date timestamp(6) without time zone,
                                    authorization_code uuid not null,
                                    id uuid primary key not null,
                                    nonce character varying(255),
                                    user_id character varying(255),
                                    client jsonb
);
create unique index ix_authorization_code on sso_request using btree (authorization_code);



