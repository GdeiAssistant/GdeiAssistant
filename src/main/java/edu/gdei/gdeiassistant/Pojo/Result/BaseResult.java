package edu.gdei.gdeiassistant.Pojo.Result;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class BaseResult<T, E> {

    private T resultData;

    private E resultType;

    public T getResultData() {
        return resultData;
    }

    public void setResultData(T resultData) {
        this.resultData = resultData;
    }

    public E getResultType() {
        return resultType;
    }

    public void setResultType(E resultType) {
        this.resultType = resultType;
    }
}
