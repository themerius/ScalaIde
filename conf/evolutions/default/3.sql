# Users schema
 
# --- !Ups
 
INSERT INTO USER (email, password, fullname, isAdmin, projectpath)
VALUES('lars@test.de', '123', 'lars', 0, 'projectspaces/lars');

INSERT INTO USER (email, password, fullname, isAdmin, projectpath)
VALUES('sven@test.de', '123', 'sven', 0, 'projectspaces/sven');

INSERT INTO USER (email, password, fullname, isAdmin, projectpath)
VALUES('simon@test.de', '123', 'simon', 0, 'projectspaces/simon');
 
# --- !Downs
 
DELETE FROM USER WHERE (EMAIL == 'lars@test.de' OR EMAIL == 'sven@test.de' OR EMAIL == 'simon@test.de');