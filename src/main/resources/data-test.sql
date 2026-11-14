-- Update adminuser to have ADMIN role
UPDATE users SET role = 'ADMIN' WHERE username = 'adminuser';
