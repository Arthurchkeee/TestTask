INSERT INTO public.users (id, date_of_birth, name, password) VALUES (1, '2000-05-01', 'John Johnson', '1234567890');
INSERT INTO public.users (id, date_of_birth, name, password) VALUES (2, '2025-05-11', 'John King', '0987654321');
INSERT INTO public.account (id, balance, initial_balance, user_id) VALUES (1, 33.00, 33.00, 1);
INSERT INTO public.account (id, balance, initial_balance, user_id) VALUES (2, 66.00, 66.00, 2);
INSERT INTO public.phone_data (id, phone, user_id) VALUES (1, '79207865432', 1);
INSERT INTO public.phone_data (id, phone, user_id) VALUES (2, '79207865433', 2);
INSERT INTO public.email_data (id, email, user_id) VALUES (1, 'JohnJohnson@gmail.com', 1);
INSERT INTO public.email_data (id, email, user_id) VALUES (2, 'JohnKing@gmail.com', 2);


ALTER SEQUENCE phone_data_id_seq RESTART WITH 3;

ALTER SEQUENCE email_data_id_seq RESTART WITH 3;