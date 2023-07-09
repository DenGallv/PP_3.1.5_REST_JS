
insert into users(name, surname, age, email, password)
values ('user', 'userov', '30', 'user@mail.ru','$2a$12$iXxOVOjg3cSRYiY/zfGbw.jeaOXKCBY/wDREZAYs8uzwYClSqtd7y');
insert into users(name, surname, age, email, password)
values ('admin', 'adminov', '35', 'admin@mail.ru','$2a$12$/VXSC9pSEA0fWdVC.YLdKeMFslCJsPZmYvhWuevnJi6zfuqY/bYTC');

insert into roles(name)
values ('ROLE_USER'),
       ('ROLE_ADMIN');

insert into users_roles(user_id, role_id)
values (1, 1);

insert into users_roles(user_id, role_id)
values (2, 2);

insert into users_roles(user_id, role_id)
values (2, 1);

