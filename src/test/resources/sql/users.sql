CREATE TABLE users (
	email text NOT NULL,
	hashedPassword text NOT NULL,
	firstName text,
	lastName text,
	company text,
	role text NOT NULL
);

ALTER TABLE users
ADD CONSTRAINT pk_users PRIMARY KEY (email);

INSERT INTO users (
	email,
	hashedPassword,
	firstName,
	lastName,
	company,
	role
) VALUES (
	'daniel@rockthejvm.com',
	'rockthejvm',
	'Daniel',
	'Ciocirlan',
	'Rock the JVM',
	'ADMIN'
);

INSERT INTO users (
	email,
	hashedPassword,
	firstName,
	lastName,
	company,
	role
) VALUES (
	'riccardo@rockthejvm.com',
	'riccardorulez',
	'Riccardo',
	'Cardin',
	'Rock the JVM',
	'RECRUITER'
);