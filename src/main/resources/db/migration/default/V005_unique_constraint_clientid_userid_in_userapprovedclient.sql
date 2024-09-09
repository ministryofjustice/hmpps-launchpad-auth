ALTER TABLE user_approved_client
ADD CONSTRAINT UNIQUE (client_id, user_id)