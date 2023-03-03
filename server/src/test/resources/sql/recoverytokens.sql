CREATE TABLE recoverytokens (
	email text NOT NULL,
	token text NOT NULL,
	expiration bigint NOT NULL
);

ALTER TABLE recoverytokens
ADD CONSTRAINT pk_recoverytokens PRIMARY KEY (email);