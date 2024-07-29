# TSA IntelliJ Idea Plugin

## How to use

- With a Gradle task `runIde` running with the following command from the root of the repo: 

    `./gradlew tsa-intellij:runIde`

- Build the plugin with Gradle task `buildPlugin` using the following command for the root of the repository:

  `./gradlew tsa-intellij:buildPlugin`
  
  and then install it via [Install Plugin from Disk...](https://www.jetbrains.com/help/idea/managing-plugins.html?_gl=1*1lrv6ku*_gcl_au*MTc2MzY3NDUwLjE3MjE5NzkzMzA.*_ga*MTE3ODA2MjEzNi4xNzA2MjY0NDc2*_ga_9J976DJZ68*MTcyMjI1NDg0MC4yMC4wLjE3MjIyNTQ4NDAuNjAuMC4w#install_plugin_from_disk) action from the ZIP file located in [build/distributions](build/distributions) dir.

Then the installed plugin could be used with either hot-keys combination `ctrl T` or `Generate TON Tests with Explyt` action
directly on the Tact/FunC/Fift file with the source code.