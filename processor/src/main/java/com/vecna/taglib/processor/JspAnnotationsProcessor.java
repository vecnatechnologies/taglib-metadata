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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Scan a package for class files and add their metadata to a taglib model   
   * @param pkg the package to scan
   * @param taglib the taglib model
   * @param loader the class loader to use
   */
  public void addLocalMetadata(String pkg, JspTaglibModel taglib, ClassLoader loader) {
    Enumeration<URL> resources;
    try {
      resources = loader.getResources(pkg.replace(".", "/"));
    } catch (IOException e) {
      s_log.warn("couldn't scan package", e);
      return;
    }

    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      String path = resource.getPath();
      s_log.debug("processing path {}", path);
      if (!path.startsWith("file:/")) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
          String[] files = dir.list();
          for (String file : files) {
            s_log.debug("file {}", file);
            if (file.endsWith(".class")) {
              String className = pkg + "." + StringUtils.removeEnd(file, ".class");
              try {
                Class<?> cls = loader.loadClass(className);
                addMetadata(cls, taglib);
              } catch (ClassNotFoundException e) {
                s_log.warn("couldn't load class", e);
              }
            }
          }
        }
      }
    }        
  }

  /**
   * Scan packages for class files and add their metadata to a taglib model   
   * @param pkg the package to scan
   * @param taglib the taglib model
   * @param loader the class loader to use
   */
  public void addLocalMetadata(String[] packages, JspTaglibModel taglib, ClassLoader loader) {
    for (String pkg : packages) {
      addLocalMetadata(pkg, taglib, loader);
    }
  }

  /**
   * Scan packages for class files and add their metadata to a taglib model using the current thread's context classloader   
   * @param pkg the package to scan
   * @param taglib the taglib model
   */
  public void addLocalMetadata(String[] packages, JspTaglibModel taglib) {
    addLocalMetadata(packages, taglib, Thread.currentThread().getContextClassLoader());
  }
}
