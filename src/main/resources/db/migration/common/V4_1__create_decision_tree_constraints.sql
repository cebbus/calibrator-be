alter table decision_tree add constraint FK7vix5sb81s8mwi97ku15m1jl5 foreign key (structure_id) references structure;
alter table decision_tree_item add constraint FKe889jprtxeduearhmwt3sm2jh foreign key (decision_tree_id) references decision_tree;
alter table decision_tree_item add constraint FK2og8n5rpat87kgk06gjvalun0 foreign key (parent_id) references decision_tree_item;