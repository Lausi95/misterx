package net.lausi95.citygame.common

data class Tenant(val value: String) {
    companion object {
        const val OVERRIDE_TENANT_HEADER_NAME = "X-TENANT-OVERRIDE"

        fun parse(host: String): Tenant {
            var x = 0
            while (x < host.length && host[x] != '.') {
                x++
            }
            val tenant = host.substring(0, x)
            return Tenant(tenant)
        }
    }
}