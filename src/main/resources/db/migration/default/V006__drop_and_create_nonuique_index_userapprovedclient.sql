DROP INDEX  ix_user_id_client_id_created_date;
create index ix_user_id_client_id_created_date on user_approved_client using btree (user_id, client_id, created_date);