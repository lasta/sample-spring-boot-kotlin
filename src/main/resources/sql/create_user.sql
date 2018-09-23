CREATE USER IF NOT EXISTS 'demo_user'@'localhost'
  IDENTIFIED BY 'demo_pass'
  PASSWORD EXPIRE NEVER;

GRANT ALL ON zipcode.* TO 'demo_user'@'localhost';