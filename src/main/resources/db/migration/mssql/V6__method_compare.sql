create table method_compare (id bigint not null, method varchar(255), test_end datetime2, test_size int, test_start datetime2, training_end datetime2, training_size int, training_start datetime2, unclassified_data_size bigint, structure_id bigint, primary key (id));