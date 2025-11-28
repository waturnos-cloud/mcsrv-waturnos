package com.waturnos.enums;

public enum ImageType {
    LOGO("logos"),
    AVATAR("avatars"),
    PROVIDER_IMAGE("providers"),
    SERVICE_IMAGE("services");
    
    private final String subdir;
    
    ImageType(String subdir) {
        this.subdir = subdir;
    }
    
    public String getSubdir() {
        return subdir;
    }
}
