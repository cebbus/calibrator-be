create sequence hibernate_sequence start with 1 increment by 1;
create table account (id bigint not null, email varchar(255), enabled bit, name varchar(255), password varchar(255), surname varchar(255), username varchar(255), primary key (id));
create table account_roles (user_id bigint not null, roles_id bigint not null, primary key (user_id, roles_id));
create table role (id bigint not null, name varchar(255), primary key (id));