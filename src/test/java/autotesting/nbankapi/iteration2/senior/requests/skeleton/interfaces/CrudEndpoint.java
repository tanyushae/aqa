package autotesting.nbankapi.iteration2.senior.requests.skeleton.interfaces;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;

public interface CrudEndpoint<R, T extends BaseModel> {
    R post(T model);

    R get();

    R get(String pathParamName, Object pathParamValue);

    R update(T model);

    R delete(String pathParamName, Object pathParamValue);
}
