import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared"))
                implementation("org.xerial:sqlite-jdbc:3.44.0.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Controle-de-Gastos"
            packageVersion = "1.0.0"
            
            // Incluir JVM no pacote para funcionar sem Java instalado
            includeAllModules = true
            
            // Configuração da JVM
            jvmArgs += listOf(
                "-Dfile.encoding=UTF-8",
                "-Xmx512m"
            )
            
            windows {
                menuGroup = "Controle de Gastos"
                shortcut = true
                // Usar bundled JVM
                dirChooser = true
                perUserInstall = true
            }
            
            linux {
                shortcut = true
            }
        }
    }
}
