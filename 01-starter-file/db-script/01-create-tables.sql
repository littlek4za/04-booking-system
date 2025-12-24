

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

CREATE TABLE users (
	id BIGSERIAL PRIMARY KEY,
	username VARCHAR(50) NOT NULL UNIQUE,
	password VARCHAR(80) NOT NULL,
	email VARCHAR(255) NOT NULL UNIQUE,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
	id SERIAL PRIMARY KEY,
	role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users_roles (
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
	PRIMARY KEY (user_id, role_id)
);

CREATE TABLE events (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	event_name TEXT NOT NULL,
	event_description TEXT,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE slots (
	id BIGSERIAL PRIMARY KEY,
	event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
	slot_name TEXT NOT NULL,
	slot_description TEXT,
	slot_start_time TIMESTAMPTZ NOT NULL,
	slot_end_time TIMESTAMPTZ NOT NULL,
	max_book INT DEFAULT 1,
	slot_type VARCHAR(20) NOT NULL, -- 'FIXED' or 'FLEXIBLE'
	slot_interval_minutes INT,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT unique_event_slot_name UNIQUE (event_id, slot_name)
);

CREATE TABLE bookings (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	slot_id BIGINT NOT NULL REFERENCES slots(id) ON DELETE CASCADE,
	booked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	booked_start_time TIMESTAMPTZ NOT NULL,
	booked_end_time TIMESTAMPTZ NOT NULL,
	is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
	deleted_at TIMESTAMPTZ,
	deleted_by VARCHAR(20)
);

CREATE TABLE invitations (
	id BIGSERIAL PRIMARY KEY,
	slot_id BIGINT NOT NULL REFERENCES slots(id) ON DELETE CASCADE,
	created_by BIGINT NOT NULL REFERENCES users(id),
	code VARCHAR(20) NOT NULL UNIQUE,
	expires_at TIMESTAMPTZ NOT NULL,
	max_usage INT DEFAULT 1,
	used_count INT DEFAULT 0,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO roles (id, role_name)
VALUES 
	(1,'ROLE_ADMIN'),
	(2,'ROLE_ORGANIZER'),
	(3,'ROLE_ATTENDEE');

INSERT INTO users (username, password, email, first_name,last_name)
VALUES 
	('admin','password','admin@test.com','booking','admin'),
	('organizer','password','organizer@test.com','booking','organizer'),
	('attendee','password','attendee@test.com','booking','attendee');

INSERT INTO users_roles (user_id, role_id)
VALUES
	(1,1),
	(2,2),
	(3,3);


