package pl.bookmarket.dto;

public class ErrorDto {
    private final String field;
    private final String errorCode;

    public ErrorDto(String field, String errorCode) {
        this.field = field;
        this.errorCode = errorCode;
    }

    public String getField() {
        return field;
    }

    public String getErrorCode() {
        return errorCode;
    }
}