package net.twistedmc.shield.Util;

public enum AssetClass {
    ICON("icon","For images that are best suited for the smaller icons."),
    THUMBNAIL("thumbnail","For images that are best suited for the bigger thumbnail or attachments."),
    VERSATILE("versatile","For images that are suited for small and big image slots.");

    private String label,desc;
    AssetClass(String label,String desc) {
        this.label = label;
        this.desc = desc;
    }

    public String getLabel() {
        return label;
    }

    public String getDesc() {
        return desc;
    }
}