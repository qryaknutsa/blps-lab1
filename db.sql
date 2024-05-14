CREATE TABLE if not exists cloud_user
(
    id           SERIAL PRIMARY KEY not null,
    username     VARCHAR(32)        not null unique,
    password     text               not null,
    role_name     VARCHAR(32)        not null,
    subscription BOOLEAN            not null,
    wallet       double precision   not null
);


ALTER TABLE cloud_user ADD CONSTRAINT c UNIQUE (username);
ALTER TABLE ownership ADD COLUMN type VARCHAR(32);
ALTER TABLE ownership ADD COLUMN filename VARCHAR(32);


CREATE TABLE if not exists ownership (
    id           SERIAL PRIMARY KEY not null,
    user_login VARCHAR(32) REFERENCES cloud_user(username),
    file_id text,
    file_type VARCHAR(32),
    filename VARCHAR(32)
);

CREATE TABLE if not exists stored_file
(
    id       SERIAL PRIMARY KEY not null,
    title    VARCHAR(32)        not null,
    data     text               not null,
    username VARCHAR(32)        not null
);



drop table ownership;
drop table stored_file;
drop table cloud_user;
