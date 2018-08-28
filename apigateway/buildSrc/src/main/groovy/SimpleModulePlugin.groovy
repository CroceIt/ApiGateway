import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPluginConvention

import java.nio.file.Path

import static ModuleDefinition.Scope.local
import static ModuleDefinition.Type.*
/**
 * @author junzheng.hu  Date: 12/17/17 Time: 23:32
 */
class SimpleModulePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (project.gradle.rootProject == project) return
        project.extensions.create("emodule", SimpleModulePluginExtension.class, project)
        def module = new ModuleDefinition(project)
        project.group = module.group
        switch (module.type) {
            case main:
                def projects = project.rootProject.allprojects.findAll {
                    it != project && it != project.rootProject
                }
                def modules = projects.collect { new ModuleDefinition(it) }
                modules.findAll {
                    it.scope == local || it.scope == ModuleDefinition.Scope.shared
                }.each { dependency ->
                    project.dependencies {
                        compile project.gradle.rootProject.project(dependency.name)
                    }
                }
                return
            case biz:
                //自动加入自己的API模块依赖
                def api = project.gradle.rootProject.findProject(module.apiName)
                if (null != api) {
                    project.dependencies {
                        compile api
                    }
                }
                break
            case api:
                break
            case thirds:
                break
            default:
                throw new IllegalStateException("无法识别模块类型！" + module);

        }

        project.afterEvaluate {
            if (!it.subprojects.isEmpty()) {
                throw new IllegalStateException("不允许模块嵌套定义！")
            }
            def sourceSet = it.convention.getPlugin(JavaPluginConvention.class).sourceSets.main
            Path output = sourceSet.java.sourceDirectories.find().toPath()
            def basePath = output.resolve(module.basePackagePath)
            sourceSet.java.each {
                Path path = it.toPath()
                if(!path.startsWith(basePath)) {
                    throw new IllegalStateException("文件" + it + " 不符合命名规范，basePackage=" + basePath)
                }
            }
            it.configurations.compile.dependencies.each { dependency ->
                if (dependency instanceof ProjectDependency) {
                    Project p = ((ProjectDependency) dependency).dependencyProject;
                    def m = new ModuleDefinition(p)
                    if (m.scope != ModuleDefinition.Scope.shared) {
                        throw new IllegalStateException("不允许依赖私有模块！path=" + p.path)
                    }
                }
            }
        }
    }
}
