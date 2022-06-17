package pl.bookmarket.testhelpers.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class EqualsId extends TypeSafeMatcher<Number> {

    private final Long id;

    public EqualsId(Long id) {
        this.id = id;
    }

    @Override
    protected boolean matchesSafely(Number item) {
        if (id == null || item == null) {
            return false;
        }
        return id == item.longValue();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Equals id");
    }

    public static EqualsId equalsId(Long id) {
        return new EqualsId(id);
    }
}