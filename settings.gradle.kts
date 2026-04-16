rootProject.name = "it-asset-management-saas"

include(":services:user-service")
project(":services:user-service").projectDir = file("services/user-service")

include(":services:asset-service")
project(":services:asset-service").projectDir = file("services/asset-service")

include(":services:inventory-service")
project(":services:inventory-service").projectDir = file("services/inventory-service")
