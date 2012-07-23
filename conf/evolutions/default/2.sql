# Users schema
 
# --- !Ups
 
INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('lars@test.de', '123', 'lars', 0, 'lars');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('sven@test.de', '123', 'sven', 0, 'sven');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('simon@test.de', '123', 'simon', 0, 'simon');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('public@test.de', '123', 'Public', 1, 'public');
 
# --- !Downs
 
DELETE FROM USER WHERE (EMAIL == 'lars@test.de' OR EMAIL == 'sven@test.de' OR EMAIL == 'simon@test.de');