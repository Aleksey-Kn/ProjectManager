package ru.manager.ProgectManager.enums;

public enum Errors {
    TOKEN_EXPIRED(1000),
    USER_WITH_THIS_LOGIN_ALREADY_CREATED(1002),
    INCORRECT_LOGIN_OR_PASSWORD(1003),
    PROJECT_ACCESS_TOKEN_IS_DEPRECATED(1004),
    PROJECT_ACCESS_TOKEN_IS_INVALID_OR_NO_LONGER_AVAILABLE(1005),
    NO_SUCH_SPECIFIED_PROJECT(1006),
    TOKEN_FOR_ACCESS_WITH_PROJECT_AS_ADMIN_MUST_BE_DISPOSABLE(1007),
    NO_SUCH_SPECIFIED_ELEMENT(1008),
    NO_SUCH_SPECIFIED_ELEMENT_OR_COLUMN(1009),
    NO_SUCH_SPECIFIED_COLUMN(1010),
    NO_SUCH_SPECIFIED_KANBAN(1011),
    NO_SUCH_SPECIFIED_USER(1012),
    NO_SUCH_SPECIFIED_COMMENT(1013),
    NO_SUCH_SPECIFIED_ATTACHMENT(1014),
    INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION(1015),
    INDEX_MORE_COLLECTION_SIZE(1100),
    BAD_FILE(1101),
    TEXT_LENGTH_IS_TOO_LONG(1102),
    NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS(2000),
    LOGIN_MUST_BE_CONTAINS_VISIBLE_SYMBOLS(2001),
    PASSWORD_MUST_BE_LONGER_3_SYMBOLS(2002),
    EMAIL_HAVE_INCORRECT_FORMAT(2003),
    NICKNAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS(2004),
    PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS(2005),
    INDEX_MUST_BE_MORE_0(2006),
    COUNT_MUST_BE_MORE_1(2007),
    TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL(2008);

    Errors(int value){
        numValue = value;
    }

    private final int numValue;

    public int getNumValue() {
        return numValue;
    }
}
