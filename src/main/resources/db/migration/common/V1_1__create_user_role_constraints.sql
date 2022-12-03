alter table account_roles add constraint FK70s9enq5d1oywl7v8vis5ke5w foreign key (roles_id) references role;
alter table account_roles add constraint FKg75jws9c251epgg4r5swxqkn3 foreign key (user_id) references account;
