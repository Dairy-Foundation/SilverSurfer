plugins {
	id("dev.frozenmilk.jvm-library") version "11.0.0-1.0.0"
	id("dev.frozenmilk.publish") version "0.0.5"
	id("dev.frozenmilk.doc") version "0.0.5"
    id("dev.frozenmilk.build-meta-data") version "0.0.2"
}

ftc {
    kotlin()
}

meta {
    packagePath = "dev.frozenmilk"
    name = "SilverSurfer"
    registerField("name", "String", "\"dev.frozenmilk.silversurfer.SilverSurfer\"")
    registerField("clean", "Boolean") { "${dairyPublishing.clean}" }
    registerField("gitRef", "String") { "\"${dairyPublishing.gitRef}\"" }
    registerField("snapshot", "Boolean") { "${dairyPublishing.snapshot}" }
    registerField("version", "String") { "\"${dairyPublishing.version}\"" }
}

publishing {
	publications {
		register<MavenPublication>("release") {
            groupId = "dev.frozenmilk"
            artifactId = "SilverSurfer"

			artifact(dairyDoc.dokkaJavadocJar)
			artifact(dairyDoc.dokkaHtmlJar)

			afterEvaluate {
				from(components["java"])
			}
		}
	}
}
