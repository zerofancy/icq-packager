repositories {
    mavenCentral()
}

val initEnvironment = tasks.create("initDebEnvironment") {
    doLast {
        Environment.init(project)
    }
}

// 准备相关资源
val prepareFile = tasks.create("prepareDebFile") {
    dependsOn(initEnvironment)
    doLast {
        download("https://hb.bizmrg.com/icq-www/linux/x64/packages/10.0.13286/icq-10.0.13286_64bit.tar.xz", "icq.tar.xz", CheckSum.Sha256("82e9a9a18a0a8b78b1ea111748904e7502e2f4176329c7b8b4af243a347d2d2a"))
        unzip("icq.tar.xz", "usr/bin")
        deleteFileOrDir("icq.tar.xz")
        download("https://hb.bizmrg.com/icq-www/linux/x64/packages/10.0.13286/icq.png", "usr/share/pixmaps/icq.png", CheckSum.Sha256("d41101994702521210a99ca95e25cba3a8fbf9ff59c1f78a4bc834f0086b3941"))
        srcFile("icq-bin.desktop", "usr/share/applications/icq-bin.desktop")
    }
}

// 生成control文件
val packageDeb = tasks.create("packageDeb") {
    dependsOn(prepareFile)
    doLast {
        generateControlFile {
            packageName("icq-bin")
            version("10.0.13286")
            mantainer("zerofancy", "ntutn.top@outlook.com")
            architecture("amd64")
            description("Official icq desktop client for Linux")
            deps {
                 dep("libxcursor1")
                 dep("libxinerama1")
                 dep("libxrandr2")
            }
            installSize(collectDirSize(""))
            homePage("https://icq.com/")
        }

        // DEBIAN文件夹权限修改755
        chmodForDEBIANDir()
        // 使用dpkg打包
        packageWithDpkg()
    }
}

tasks.create("build") {
    dependsOn(packageDeb)
}

tasks.create("clean") {
    doLast {
        project.buildDir.deleteRecursively()
    }
}