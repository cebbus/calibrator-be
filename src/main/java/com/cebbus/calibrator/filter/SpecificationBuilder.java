package com.cebbus.calibrator.filter;

import com.cebbus.calibrator.common.JsonOperations;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {

    private final Class<T> type;
    private final List<SearchCriteria> params;

    public SpecificationBuilder() {
        this(null);
    }

    public SpecificationBuilder(Class<T> type) {
        this.type = type;
        this.params = new ArrayList<>();
    }

    public SpecificationBuilder<T> with(String filter) {
        return with(JsonOperations.stringToList(filter, SearchCriteria.class));
    }

    public SpecificationBuilder<T> with(List<SearchCriteria> criteriaList) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return this;
        }

        params.addAll(criteriaList);
        return this;
    }

    public SpecificationBuilder<T> with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) {
            SearchCriteria criteria = new SearchCriteria("id", "gt", 0);
            return Specification.where(new DataSpecification<>(criteria));
        }

        List<Specification<T>> specs = new ArrayList<>();
        for (SearchCriteria param : params) {
            specs.add(new DataSpecification<>(param));
        }

        Specification<T> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }

        return result;
    }
}
