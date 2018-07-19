package com.hjzgg.apigateway.swagger.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;

public class CodeRepository {

  private static final CodeRepository instance = new CodeRepository();
  private static final Map<Member, String[]> EMPTY_MAP = Collections.emptyMap();

  private ConcurrentMap<String, CompilationUnit> cache = new ConcurrentHashMap<>();

  private CodeRepository() {
  }

  public static CodeRepository getInstance() {
    return instance;
  }

  public Map<Member, String[]> getMethodParameterMaps(Class<?> clazz) {
    Map<Member, String[]> cache = new HashMap<>();
    return getType(clazz).map(typeDec -> {
      for (MethodDeclaration methodDec : typeDec.getMethods()) {
        Method method = ClassUtils
            .getMethodIfAvailable(clazz, methodDec.getNameAsString(), null);
        NodeList<Parameter> parameters = methodDec.getParameters();
        String[] names = new String[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
          names[i] = parameters.get(i).getNameAsString();
        }
        cache.put(method, names);
      }
      return cache;
    }).orElse(EMPTY_MAP);
  }

  public Optional<Javadoc> getJavaDoc(Class<?> type) {
    Optional<TypeDeclaration<?>> typeDeclaration = getType(type);
    if (typeDeclaration.isPresent()) {
      Optional<Javadoc> javadoc = typeDeclaration.get().getJavadoc();
      return _getDoc(javadoc);
    }
    return Optional.empty();
  }

  private Optional<Javadoc> _getDoc(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Javadoc> javadoc) {
    if (!javadoc.isPresent()) {
      return Optional.empty();
    }
    if (javadoc.get().getDescription().isEmpty()) {
      return Optional.empty();
    }
    return javadoc;
  }

  public Optional<TypeDeclaration<?>> getType(Class<?> type) {
    //假定每个类型或者接口都定义在独立的文件中
    CompilationUnit unit = cache.computeIfAbsent(type.getName(), name -> parse(type));
    if (null == unit) {
      return Optional.empty();
    }
    return unit.getTypes().stream()
        .filter(t -> t.getNameAsString().equals(type.getSimpleName())).findFirst();
  }

  private CompilationUnit parse(Class<?> declaringClass) {
    String className = declaringClass.getName();
    int lastDotIndex = className.lastIndexOf('.');
    String sourceFileName = className.substring(lastDotIndex + 1) + ".java";
    InputStream resource = declaringClass.getResourceAsStream(sourceFileName);
    if (null == resource) {
      return null;
    }
    try {
      return JavaParser.parse(resource);
    } finally {
      try {
        resource.close();
      } catch (IOException e) {
        //ignored
      }
    }
  }

  public Optional<Javadoc> getJavaDoc(Member member) {
    Optional<TypeDeclaration<?>> type = getType(member.getDeclaringClass());
    if (!type.isPresent()) {
      return Optional.empty();
    }
    TypeDeclaration<?> typeDeclaration = type.get();
    if (member instanceof Method || member instanceof Constructor) {
      List<MethodDeclaration> methodsByName = typeDeclaration.getMethodsByName(member.getName());
      if (!methodsByName.isEmpty()) {
        MethodDeclaration methodDeclaration = methodsByName.get(0);
        return _getDoc(methodDeclaration.getJavadoc());
      }
    }
    if (member instanceof Field) {
      Optional<FieldDeclaration> fieldByName = typeDeclaration.getFieldByName(member.getName());
      if (fieldByName.isPresent()) {
        return _getDoc(fieldByName.get().getJavadoc());
      }
    }
    return Optional.empty();
  }

  public String getMethodParameterDoc(MethodParameter methodParameter) {
    Optional<Javadoc> methodDoc = getJavaDoc(methodParameter.getMethod());
    if (!methodDoc.isPresent()) {
      return "<无@param注释>";
    }
    Javadoc javadoc = methodDoc.get();
    for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
      if (blockTag.getType() != Type.PARAM) {
        continue;
      }
      Optional<String> name = blockTag.getName();
      if (!name.isPresent()) {
        return "<@param缺少参数名称>";
      }
      if (name.get().equals(methodParameter.getParameterName())) {
        return blockTag.getContent().toText();
      }
    }
    return "<无@Param注释>";
  }
}
