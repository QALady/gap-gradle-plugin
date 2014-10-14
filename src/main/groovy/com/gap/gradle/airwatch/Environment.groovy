package com.gap.gradle.airwatch

class Environment {
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
