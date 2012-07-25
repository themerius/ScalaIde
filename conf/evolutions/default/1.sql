# Users schema
 
# --- !Ups
 
CREATE TABLE User (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    fullname varchar(255) NOT NULL,
    sshlogin varchar(255)  DEFAULT '',
    projectpath varchar(255) NOT NULL,
    PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE User;