/**
 * Copyright 2011 Vecna Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
*/

package com.vecna.taglib.processor;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vecna.taglib.annotations.JspAttribute;
import com.vecna.taglib.annotations.JspFunction;
import com.vecna.taglib.annotations.JspTag;
import com.vecna.taglib.annotations.JspVariable;
import com.vecna.taglib.model.JspAttributeModel;
import com.vecna.taglib.model.JspFunctionModel;
import com.vecna.taglib.model.JspFunctionSignature;
import com.vecna.taglib.model.JspTagModel;
import com.vecna.taglib.model.JspTaglibModel;
import com.vecna.taglib.model.JspVariableModel;

/**
 * Builds JSP taglib model from annotated classes and tag files
 * @author ogolberg@vecna.com
 */
public class JspAnnotationsProcessor {
  private static final Logger s_log = LoggerFactory.getLogger(JspAnnotationsProcessor.class);

  private static final String CLASS_NAME_SUFFIX = ".class";
  private static final String FILE_URL_PREFIX = "file:";
  private static final String NESTED_FILE_URL_SEPARATOR = "!";
  private static final String PACKAGE_SEPARATOR = ".";
  private static final String JAR_PATH_SEPARATOR = "/";

  /**
   * Build a JSP tag model object from an annotated tag class
   * @param type tag class
   * @return JSP tag model
   */
  public JspTagModel getTagMetadata(Class<?> type) {
    JspTagModel metadata = new JspTagModel();
    JspTag jspTagAnnotation = type.getAnnotation(JspTag.class);
    metadata.bodyContent = jspTagAnnotation.bodyContent();
    metadata.name = jspTagAnnotation.name();
    metadata.tagClass = type.getName();
    metadata.dynamicAttributes = jspTagAnnotation.dynamicAttributes();

    for (JspVariable jspVariableAnnotation : jspTagAnnotation.variables()) {
      JspVariableModel variable = new JspVariableModel();
      variable.declare = jspVariableAnnotation.declare();

      variable.nameFromAttribute = StringUtils.stripToNull(jspVariableAnnotation.nameFromAttribute());
      variable.nameGiven = StringUtils.stripToNull(jspVariableAnnotation.nameGiven());

      variable.scope = jspVariableAnnotation.scope();
      variable.variableClass = jspVariableAnnotation.variableClass().getName();
      metadata.variables.add(variable);
    }

    for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(type)) {
      s_log.debug("processing property {}", pd.getName());
      if (pd.getWriteMethod() != null && pd.getWriteMethod().isAnnotationPresent(JspAttribute.class)) {
        s_log.debug("attribute metadata present on {}", pd.getName());
        JspAttributeModel attr = new JspAttributeModel();
        attr.name = pd.getName();
        attr.type = pd.getPropertyType().getName();
        JspAttribute jspAttributeAnnotation = pd.getWriteMethod().getAnnotation(JspAttribute.class);
        attr.required = jspAttributeAnnotation.required();
        attr.rtExprValue = jspAttributeAnnotation.rtExprValue();
        attr.fragment = jspAttributeAnnotation.fragment();

        attr.deferredMethod = jspAttributeAnnotation.deferredMethod() ? true : null;
        attr.deferredValue = jspAttributeAnnotation.deferredValue() ? true : null;

        metadata.attributes.add(attr);
      }
    }

    return metadata;
  }

  /**
   * Build JSP function models from an annotated class
   * @param type class with annotated static methods
   * @return JSP function models
   */
  public Collection<JspFunctionModel> getFunctionMetadata(Class<?> type) {
    Collection<JspFunctionModel> functions = new ArrayList<JspFunctionModel>();
    for (Method method: type.getMethods()) {
      if (Modifier.isStatic(method.getModifiers())
          && Modifier.isPublic(method.getModifiers())
          && method.isAnnotationPresent(JspFunction.class)) {
        JspFunction functionAnnotation = method.getAnnotation(JspFunction.class);
        JspFunctionModel metadata = new JspFunctionModel();
        metadata.name = functionAnnotation.name();
        metadata.functionClass = type.getName();
        JspFunctionSignature signature = new JspFunctionSignature();
        signature.name = method.getName();
        signature.argumentTypes = method.getParameterTypes();
        signature.returnType = method.getReturnType();
        metadata.signature = signature;
        functions.add(metadata);
      }
    }
    return functions;
  }

  /**
   * Adds taglib metadata from an annotated class to a taglib model
   * @param type annotated class
   * @param taglib taglib model
   */
  public void addMetadata(Class<?> type, JspTaglibModel taglib) {
    if (type.isAnnotationPresent(JspTag.class)) {
      taglib.tags.add(getTagMetadata(type));
    }
    taglib.functions.addAll(getFunctionMetadata(type));
  }

  /**
   * Scan a classloader for classes under the given package.
   * @param pkg package name
   * @param loader classloader
   * @param lookInsideJars whether to consider classes inside jars or only "unpacked" class files
   * @return matching class names (will not attemp to actually load these classes)
   */
  private Collection<String> scanClasspath(String pkg, ClassLoader loader, boolean lookInsideJars) {
    Collection<String> classes = Lists.newArrayList();

    Enumeration<URL> resources;
    String packageDir = pkg.replace(PACKAGE_SEPARATOR, JAR_PATH_SEPARATOR) + JAR_PATH_SEPARATOR;

    try {
      resources = loader.getResources(packageDir);
    } catch (IOException e) {
      s_log.warn("couldn't scan package", e);
      return classes;
    }

    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      String path = resource.getPath();
      s_log.debug("processing path {}", path);

      if (path.startsWith(FILE_URL_PREFIX)) {
        if (lookInsideJars) {
          String jarFilePath = StringUtils.substringBetween(path, FILE_URL_PREFIX, NESTED_FILE_URL_SEPARATOR);
          try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
              String entryName = entries.nextElement().getName();
              if (entryName.startsWith(packageDir) && entryName.endsWith(CLASS_NAME_SUFFIX)) {
                String potentialClassName = entryName.substring(packageDir.length(),
                                                                entryName.length() - CLASS_NAME_SUFFIX.length());
                if (!potentialClassName.contains(JAR_PATH_SEPARATOR)) {
                  classes.add(pkg + PACKAGE_SEPARATOR + potentialClassName);
                }
              }
            }
          } catch (IOException e) {
            s_log.warn("couldn't open jar file", e);
          }
        }
      } else {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
          String[] files = dir.list();
          for (String file : files) {
            s_log.debug("file {}", file);
            if (file.endsWith(CLASS_NAME_SUFFIX)) {
              classes.add(pkg + PACKAGE_SEPARATOR + StringUtils.removeEnd(file, CLASS_NAME_SUFFIX));
            }
          }
        }
      }
    }
    return classes;
  }

  /**
   * Scan a package for class files and add their metadata to a taglib model
   * @param pkg the package to scan
   * @param taglib the taglib model
   * @param loader the class loader to use
   * @param lookInsideJars whether to look inside jars
   */
  public void addLocalMetadata(String pkg, JspTaglibModel taglib, ClassLoader loader, boolean lookInsideJars) {
    Collection<String> classes = scanClasspath(pkg, loader, lookInsideJars);
    for (String className : classes) {
      try {
        Class<?> cls = loader.loadClass(className);
        addMetadata(cls, taglib);
      } catch (ClassNotFoundException e) {
        s_log.warn("couldn't load class", e);
      }
    }
  }

  /**
   * Scan packages for class files and add their metadata to a taglib model
   * @param pkg the package to scan
   * @param taglib the taglib model
   * @param loader the class loader to use
   * @param lookInsideJars whether to look for classes inside jars
   */
  public void addLocalMetadata(String[] packages, JspTaglibModel taglib, ClassLoader loader, boolean lookInsideJars) {
    for (String pkg : packages) {
      addLocalMetadata(pkg, taglib, loader, lookInsideJars);
    }
  }

  /**
   * Scan packages for class files and add their metadata to a taglib model using the current thread's context classloader
   * @param pkg the package to scan
   * @param taglib the taglib model
   * @param lookInsideJars whether to look inside jars for class files
   */
  public void addLocalMetadata(String[] packages, JspTaglibModel taglib, boolean lookInsideJars) {
    addLocalMetadata(packages, taglib, Thread.currentThread().getContextClassLoader(), lookInsideJars);
  }
}
