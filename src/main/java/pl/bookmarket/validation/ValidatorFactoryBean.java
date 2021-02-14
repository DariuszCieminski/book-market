package pl.bookmarket.validation;

import javax.validation.Configuration;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class ValidatorFactoryBean extends LocalValidatorFactoryBean {

    /*
        Use custom property node name provider to allow resolving property names from Jackson annotations during validation
        
        More details here:
        https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-property-node-name-provider
    */
    @Override
    protected void postProcessConfiguration(Configuration<?> configuration) {
        if (configuration instanceof HibernateValidatorConfiguration) {
            CustomPropertyNodeNameProvider provider = new CustomPropertyNodeNameProvider();

            ((HibernateValidatorConfiguration) configuration).propertyNodeNameProvider(provider);
        }

        super.postProcessConfiguration(configuration);
    }
}