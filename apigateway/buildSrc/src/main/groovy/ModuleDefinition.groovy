/**
 * @author junzheng.hu  Date: 12/17/17 Time: 23:35
 */
class ModuleDefinition {
    public static final String MODULE_DEPENDENCIES = "moduleDependencies"

    enum Scope {
        shared, local, none
    }

    enum Type {
        main, biz, api, thirds
    }

    final Scope scope
    final Type type
    final String group
    final String name
    final String version
    final String apiName
    final String basePackagePath

    ModuleDefinition(project) {
        this.name = project.path
        this.apiName = this.name + "-api"
        this.version = project.version
        def properties = project.properties
        if (name.endsWith("-api")) {
            scope = Scope.shared
            type = Type.api
        } else if (name.endsWith("-main")) {
            scope = Scope.none
            type = Type.main
        } else if (name.endsWith("-model")) {
            scope = Scope.shared
            type = Type.biz
        } else if (name.endsWith("-dubbo")
                || name.endsWith("-commons")
                || name.endsWith("-beans")
                || name.endsWith("-datasource")) {
            scope = Scope.shared
            type = Type.thirds
        } else {
            scope = Scope.local
            type = Type.biz
        }
        //com.hjzgg.scm
        String groupPrefix = properties.groupPrefix as String
        //tms
        String groupName = properties.groupName as String
        //com.hjzgg.scm.tms
        this.group = groupPrefix + "." + groupName
        if (!this.name.startsWith(":" + groupName)) {
            throw new IllegalStateException("模块名称必须以'" + groupName + "-'开头")
        }
        this.basePackagePath = groupPrefix.replace('.', '/') + this.name.replace(':', '/').replace('-', '/')
    }

    @Override
    String toString() {
        return "ModuleDefinition{" +
                "scope=" + scope +
                ", type=" + type +
                ", group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
