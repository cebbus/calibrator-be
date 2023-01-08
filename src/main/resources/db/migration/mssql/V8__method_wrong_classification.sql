alter table method_compare drop column unclassified_data_size;
go

alter table method_compare add unclassified_data_size int null;
alter table method_compare add wrong_classified_data_size int null;