# Users schema
 
# --- !Ups
 
INSERT INTO USER (email, password, fullname, projectpath)
VALUES('lars@test.de', '123', 'lars', 'projectspaces/lars');

INSERT INTO USER (email, password, fullname, projectpath)
VALUES('sven@test.de', '123', 'sven', 'projectspaces/sven');

INSERT INTO USER (email, password, fullname, projectpath)
VALUES('simon@test.de', '123', 'simon', 'projectspaces/simon');

INSERT INTO USER (email, password, fullname, projectpath)
VALUES('public@test.de', '123', 'Public', 'projectspaces/public');

INSERT INTO USER (email, password, fullname, sshlogin, projectpath)
VALUES('absolut@test.de', '123', 'AboluteUser', 'terminal@141.37.31.235', '/home/terminal');
 
# --- !Downs
 
DELETE FROM USER WHERE (
  EMAIL == 'lars@test.de' OR 
  EMAIL == 'sven@test.de' OR 
  EMAIL == 'simon@test.de' OR 
  EMAIL == 'public@test.de' OR 
  EMAIL == 'absolut@test.de'
);