package ru.sentidas.rangiffler;

import java.util.Set;

public class MediaProperties {
    private Set<String> allowedMime;
    public Set<String> getAllowedMime() { return allowedMime; }
    public void setAllowedMime(Set<String> allowedMime) { this.allowedMime = allowedMime; }
}
