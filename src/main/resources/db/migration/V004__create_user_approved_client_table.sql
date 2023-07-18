create table public.user_approved_client (
                                             created_date timestamp(6) without time zone not null,
                                             last_modified_date timestamp(6) without time zone not null,
                                             client_id uuid not null,
                                             id uuid primary key not null,
                                             user_id character varying(255) not null,
                                             scopes jsonb not null
);
create unique index ix_user_id_client_id on user_approved_client using btree (user_id, client_id);


