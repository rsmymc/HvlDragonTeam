package com.hvl.dragonteam.Model;

public class FilterModel {

    boolean left = true;
    boolean right= true;
    boolean both = true;
    boolean hideDontAttend = false;
    boolean hideImage = false;

    public FilterModel() {
    }

    public FilterModel(boolean left, boolean right, boolean both, boolean hideDontAttend, boolean hideImage) {
        this.left = left;
        this.right = right;
        this.both = both;
        this.hideDontAttend = hideDontAttend;
        this.hideImage = hideImage;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isBoth() {
        return both;
    }

    public void setBoth(boolean both) {
        this.both = both;
    }

    public boolean isHideDontAttend() {
        return hideDontAttend;
    }

    public void setHideDontAttend(boolean hideDontAttend) {
        this.hideDontAttend = hideDontAttend;
    }

    public boolean isHideImage() {
        return hideImage;
    }

    public void setHideImage(boolean hideImage) {
        this.hideImage = hideImage;
    }
}
