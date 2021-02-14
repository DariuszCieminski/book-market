package pl.bookmarket.validation;

import javax.validation.groups.Default;

public class ValidationGroups {

    //for UniqueRegisterData constraint, that needs to be checked only on account creation
    public interface CreateUser extends Default {}

    //checks if the message receiver is not null only, if user sends new message themself
    public interface SendMessage extends Default {}
}