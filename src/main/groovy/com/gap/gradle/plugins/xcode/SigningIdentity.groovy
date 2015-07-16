package com.gap.gradle.plugins.xcode

class SigningIdentity {
    public static final def DEFAULT_DEVELOPMENT = new SigningIdentity('development').with {
        description = 'iPhone Developer: Alan Orchaton (DY23J3NF52)'
        certificateURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/DevPrivateKey.p12'
        certificatePassword = 'jlohr1'
        mobileProvisionURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/GapDevelopment.mobileprovision'
        return it
    }

    public static final def DEFAULT_DISTRIBUTION = new SigningIdentity('distribution').with {
        description = 'iPhone Distribution: Gap Inc.'
        certificateURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/InternalDistKeyv2.p12'
        certificatePassword = 'jlohr1'
        mobileProvisionURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/GapInternalDistributionV4.mobileprovision'
        return it
    }

    public static final def DEFAULT_DISTRIBUTION_IOS8 = new SigningIdentity('distribution').with {
        description = 'iPhone Distribution: Gap Inc.'
        certificateURI = 'http://github.gapinc.dev/mpl/ios-code-signing/blob/master/RISProdCertKey.p12'
        certificatePassword = 'tickl3m3pink!'
        mobileProvisionURI = 'http://github.gapinc.dev/mpl/ios-code-signing/blob/master/GapInternalDistribution_ios8.mobileprovision'
        return it
    }

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
