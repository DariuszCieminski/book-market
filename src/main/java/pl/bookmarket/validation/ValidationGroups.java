package pl.bookmarket.validation;

import javax.validation.groups.Default;

public class ValidationGroups {

    public interface OnCreate extends Default {
    }

    public interface OnUpdate extends Default {
    }

    public interface OnRegister extends Default {
    }
}