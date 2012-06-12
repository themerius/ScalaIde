# Users schema
 
# --- !Ups
 
ALTER TABLE User ADD projectpath varchar(255) NOT NULL;
 
# --- !Downs
 
ALTER TABLE User DROP projectpath;