import org.gradle.api.Project

/**
 * @author junzheng.hu  Date: 12/19/17 Time: 00:58
 */
class SimpleModulePluginExtension {
    private final Project project
    private final ModuleDefinition module

    SimpleModulePluginExtension(Project project) {
        this.project = project
        this.module = new ModuleDefinition(project)
    }

    private void selfCheck() {
        if (this.module.type == ModuleDefinition.Type.main || this.module.type == ModuleDefinition.Type.api) {
            throw new IllegalStateException("模块不能定义依赖！(module=" + this.module + ")")
        }
    }

    private ModuleDefinition createModule(Project dependency) {
        selfCheck()
        if (dependency == this.project) {
            throw new IllegalStateException("不能依赖自己! path=" + path)
        }
        return new ModuleDefinition(dependency)
    }

    private void addTestDependency(String path) {
        def dep = this.project.gradle.rootProject.project(path)
        this.project.dependencies {
            testCompile dep
        }
    }

    private void addDependency(String path) {
        def dep = this.project.gradle.rootProject.project(path)
        this.project.dependencies {
            compile dep
        }
    }

    void dependency(Project project) {
        def module = createModule(project)
        switch (module.scope) {
            case ModuleDefinition.Scope.shared:
                addDependency(module.name)
                break
            case ModuleDefinition.Scope.local:
                addDependency(module.apiName)
                addTestDependency(module.name)
                break
            default:
                throw new IllegalStateException("无法处理依赖，module=" + module)

        }
    }

    void dependency(String moduleName) {
        this.dependency(this.project.gradle.rootProject.project(":" + moduleName))
    }
}
