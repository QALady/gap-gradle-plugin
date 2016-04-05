package com.gap.gradle.plugins.xcode

import com.gap.gradle.utils.EncryptedString
import com.gap.gradle.utils.EncryptionUtil

class SigningIdentity {

    private EncryptionUtil util = new EncryptionUtil()

    public static final def DEFAULT_DEVELOPMENT = new SigningIdentity('development').with {
        description = 'iPhone Developer: Zakeeruddin Mohammed (7NKV3X7B6B)'
        certificateURI = 'http://github.gapinc.dev/Za3i0u5/ios-code-signing/raw/master/DevPrivateKey_2016.p12'
        certificatePassword = new EncryptedString("ENC(hKiNy38PLtEoxfaZfzEdGP52/TmI9CiE)", util.jasyptKey).decrypt()
        mobileProvisionURI = 'http://github.gapinc.dev/Za3i0u5/ios-code-signing/raw/master/DevelopmentProvisionProv_Sep29.mobileprovision'
        return it
    }

    public static final def DEFAULT_DISTRIBUTION = new SigningIdentity('distribution').with {
        description = 'iPhone Distribution: Gap Inc.'
        certificateURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/ProdSigningCert.p12'
        certificatePassword = new EncryptedString("ENC(oszKgh+kh9Ew6+bZ+b3ocWzEAUF4azPB)", util.jasyptKey).decrypt()
        mobileProvisionURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/GapInternalDistribution_2015.mobileprovision'
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
