# Users schema
 
# --- !Ups
 
INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('lars@test.de', '123', 'lars', 0, 'projectspaces/lars');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('sven@test.de', '123', 'sven', 0, 'projectspaces/sven');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('simon@test.de', '123', 'simon', 0, 'projectspaces/simon');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('public@test.de', '123', 'Public', 1, 'projectspaces/public');

INSERT INTO USER (email, password, fullname, public, projectpath)
VALUES('absolut@test.de', '123', 'AboluteUser', 0, '/home/terminal');
 
# --- !Downs
 
DELETE FROM USER WHERE (EMAIL == 'lars@test.de' OR EMAIL == 'sven@test.de' OR EMAIL == 'simon@test.de' OR EMAIL == "public@test.de");