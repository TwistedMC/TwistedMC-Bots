package net.twistedmc.shield.Util;

public enum Images {
    MabIcon("mab icon",AssetClass.ICON,"https://twistedmcstudios.com/images/MABLogo.png");





    private String label,ImgURL;
    private AssetClass assetClass;

    Images(String label, AssetClass assetClass, String URL) {
        this.label = label;
        this.assetClass = assetClass;
        this.ImgURL = URL;
    }

    public String getLabel() {
        return label;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public String getImgURL() {
        return ImgURL;
    }


}
