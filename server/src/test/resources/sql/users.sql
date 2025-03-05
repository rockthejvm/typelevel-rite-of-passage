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
	'$2a$10$jY60jL/9Lv6./UHhhj2ZvOSm8PQIiTueC4gmsegrD5K.Yi6/mGY.m',
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
	'$2a$10$jDPXCNCHkbZLzmiTRuw9A.gBHRDQ1iKnYONCBuskyOln8Aa8eucFa',
	'Riccardo',
	'Cardin',
	'Rock the JVM',
	'RECRUITER'
);