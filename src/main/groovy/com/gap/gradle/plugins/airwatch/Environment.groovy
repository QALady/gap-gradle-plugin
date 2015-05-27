package com.gap.gradle.plugins.airwatch

class Environment {
    public static final def DEFAULT_PRODUCTION = new Environment("production").with {
        apiHost = "https://gapstoresds.awmdm.com/"
        consoleHost = "https://gapstoresds.awmdm.com/"
        tenantCode = "1VOJHIBAAAG6A46QCFAA"
        credentialName = "AirWatchProd"
        locationGroupId = "570"
        return it
    }

    public static final def DEFAULT_PREPRODUCTION = new Environment("preProduction").with {
        //apiHost = "https://cn377.awmdm.com/"
        //consoleHost = "https://cn377.awmdm.com/"
        //tenantCode = "1AVBHIBAAAG6A4NQCFAA"
        //credentialName = "AirWatchPreProd"
        //locationGroupId = "570"
        apiHost = "https://gapstorescn.awmdm.com/"
        consoleHost = "https://gapstorescn.awmdm.com/"
        tenantCode = "1WZTHMBAAAG7A6JAAEQA"
        credentialName = "AirWatchPreProd"
        locationGroupId = "620"
        return it
    }

    final String name
    String apiHost
    String consoleHost
    String tenantCode
    String locationGroupId
    String credentialName

    public Environment(String name) {
        this.name = name
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Environment that = (Environment) o

        return name == that.name
    }

    @Override
    int hashCode() {
        return name.hashCode()
    }

    @Override
    public String toString() {
        return "Environment{" +
                "name='" + name + '\'' +
                ", apiHost='" + apiHost + '\'' +
                ", consoleHost='" + consoleHost + '\'' +
                ", tenantCode='" + tenantCode + '\'' +
                ", locationGroupId='" + locationGroupId + '\'' +
                ", credentialName='" + credentialName + '\'' +
                '}';
    }
}
