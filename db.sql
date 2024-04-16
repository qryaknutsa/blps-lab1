CREATE TABLE if not exists cloud_user
(
    id           SERIAL PRIMARY KEY not null,
    username     VARCHAR(32)        not null,
    password     text               not null,
    role_name     VARCHAR(32)        not null,
    subscription BOOLEAN            not null,
    wallet       double precision   not null
);

CREATE TABLE if not exists stored_file
(
    id       SERIAL PRIMARY KEY not null,
    title    VARCHAR(32)        not null,
    data     text               not null,
    username VARCHAR(32)        not null
);



drop table stored_file;
drop table cloud_user;
