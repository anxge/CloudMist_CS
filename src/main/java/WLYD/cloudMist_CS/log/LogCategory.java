package WLYD.cloudMist_CS.log;

public enum LogCategory {
    GAME("game"),
    ECONOMY("economy"),
    WEAPON("weapon"),
    ERROR("error"),
    DEBUG("debug"),
    PERFORMANCE("performance");

    private final String categoryName;

    LogCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
