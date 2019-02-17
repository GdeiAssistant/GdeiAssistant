package edu.gdei.gdeiassistant.Pojo.KaoYanQuery;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

public class KaoYanQuery {

    @NotBlank
    @Length(min = 1, max = 15)
    private String name;

    @NotBlank
    @Length(min = 18, max = 18)
    @Pattern(regexp = "^((\\d{18})|([0-9x]{18})|([0-9X]{18}))$")
    private String idNumber;

    @NotBlank
    @Length(min = 15, max = 15)
    @Pattern(regexp = "^[0-9]*$")
    private String examNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getExamNumber() {
        return examNumber;
    }

    public void setExamNumber(String examNumber) {
        this.examNumber = examNumber;
    }
}
