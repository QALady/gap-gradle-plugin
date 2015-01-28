package com.gap.gradle.plugins.xcode

class SigningIdentity {
    final String name
    String description
    String certificateURI
    String certificatePassword
    String mobileProvisionURI

    SigningIdentity(String name) {
        this.name = name
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SigningIdentity that = (SigningIdentity) o

        return name == that.name
    }

    @Override
    int hashCode() {
        return name.hashCode()
    }

    @Override
    public String toString() {
        return "SigningIdentity{" +
                "description='" + description + '\'' +
                ", certificateURI='" + certificateURI + '\'' +
                ", mobileProvisionURI='" + mobileProvisionURI + '\'' +
                '}';
    }
}
