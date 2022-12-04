alter table structure add constraint UKmsyp18ov684gd211ey0ykwstg unique (class_name);
alter table structure add constraint UKcxvjh4skhxel972wqxlpm1chq unique (table_name);
alter table structure_field add constraint UK7mecg9fkretrgkjxeueqbu4qs unique (field_name, structure_id);
alter table structure_field add constraint UKaki3c3etd07eyp3tyd8epc3yi unique (column_name, structure_id);
alter table structure_field add constraint FK3ee4uitpcsptginx4k2xbsw89 foreign key (structure_id) references structure;
