package entity.enums;

public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-");

    private final String display;

    BloodType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }

    public static BloodType fromString(String text) {
        for (BloodType bt : BloodType.values()) {
            if (bt.display.equalsIgnoreCase(text)) {
                return bt;
            }
        }
        throw new IllegalArgumentException("Invalid blood type: " + text);
    }
}
