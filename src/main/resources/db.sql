CREATE DATABASE mini_dish_db;
CREATE USER mini_dish_db_manager WITH PASSWORD '123456';
GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;

\c mini_dish_db;

-- Privileges to work on public schema
GRANT CREATE ON SCHEMA public TO mini_dish_db_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO mini_dish_db_manager;


