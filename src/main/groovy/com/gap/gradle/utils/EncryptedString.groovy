package com.gap.gradle.utils

import lombok.EqualsAndHashCode
import lombok.ToString
import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig
import org.jasypt.properties.PropertyValueEncryptionUtils

import static org.jasypt.properties.PropertyValueEncryptionUtils.isEncryptedValue

/**
 * Created by mohsinroowalla on 8/22/15.
 *
 **/
@ToString
@EqualsAndHashCode
class EncryptedString {

    private final StringEncryptor encryptor;

    private static final String  ENCRYPTION_ALGORITHM = "PBEWITHMD5ANDDES";

    private final String jasyptPassword;
    private final String value;

    public EncryptedString (String value, String jasyptPassword) {
        this.value = value;
        this.jasyptPassword = jasyptPassword;
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(ENCRYPTION_ALGORITHM);
        config.setPassword(jasyptPassword);

        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setConfig(config);
        encryptor = standardPBEStringEncryptor;
    }

    public String decrypt() {
        return isEncryptedValue(value) ? PropertyValueEncryptionUtils.decrypt(value, encryptor) : value;
    }
}
