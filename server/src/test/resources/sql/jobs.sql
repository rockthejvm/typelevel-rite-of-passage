CREATE TABLE jobs(
	id uuid DEFAULT gen_random_uuid(),
	date bigint NOT NULL,
	ownerEmail text NOT NULL,
	company text NOT NULL,
	title text NOT NULL,
	description text NOT NULL,
	externalUrl text NOT NULL,
	remote boolean NOT NULL DEFAULT false,
	location text,
	salaryLo integer,
	salaryHi integer,
	currency text,
	country text,
	tags text[],
	image text,
	seniority text,
	other text,
	active BOOLEAN NOT NULL DEFAULT false
);

ALTER TABLE jobs
ADD CONSTRAINT pk_jobs PRIMARY KEY (id);

INSERT INTO jobs(
	id,
	date,
	ownerEmail,
	company,
	title,
	description,
	externalUrl,
	remote,
	location,
	salaryLo,
	salaryHi,
	currency,
	country,
	tags,
	image,
	seniority,
	other,
	active
) VALUES (
	'843df718-ec6e-4d49-9289-f799c0f40064', -- id
	1659186086, -- date
	'daniel@rockthejvm.com', -- ownerEmail
	'Awesome Company', -- company
	'Tech Lead', -- title
	'An awesome job in Berlin', -- description
	'https://rockthejvm.com/awesomejob', -- externalUrl
	false, -- remote
	'Berlin', -- location
	2000, -- salaryLo
	3000, -- salaryHi
	'EUR', -- currency
	'Germany', -- country
	ARRAY [ 'scala', 'scala-3', 'cats' ], -- tags
	NULL, -- image
	'Senior', -- seniority
	NULL, -- other
	true -- active
);
