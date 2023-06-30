create table public.sso_request
(
    id                 uuid not null
        primary key,
    authorization_code uuid,
    client             jsonb,
    created_date       timestamp(6) not null ,
    nonce              varchar(255) not null,
    user_id            varchar(255)
);

alter table public.sso_request
    owner to postgres;

