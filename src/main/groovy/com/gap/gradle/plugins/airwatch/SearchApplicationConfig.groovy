package com.gap.gradle.plugins.airwatch

class SearchApplicationConfig {
    String bundleId
    String status
    String applicationName
    String category
    String locationGroupId
    String platform
    String model
    String page
    String pageSize
    String type
    String applicationType

    public String toString() {
        return "Search Parameters" +
                "\n bundleId: " + bundleId +
                "\n status: " + status +
                "\n applicationName: " + applicationName
    }

    Map validSearchEndPointParams() {
        def values = ['bundleid'       : this.bundleId,
                      'status'         : this.status,
                      'applicationname': this.applicationName,
                      'category'       : this.category,
                      'locationgroupid': this.locationGroupId,
                      'platform'       : this.platform,
                      'model'          : this.model,
                      'page'           : this.page,
                      'pagesize'       : this.pageSize,
                      'type'           : this.type,
                      'applicationtype': this.applicationType]

        values.findAll { key, value -> value != null }
    }

    boolean hasSearchParams() {
        !validSearchEndPointParams().isEmpty()
    }
}
