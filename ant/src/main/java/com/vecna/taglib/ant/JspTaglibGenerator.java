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

package com.vecna.taglib.ant;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.vecna.taglib.model.JspTaglibModel;
import com.vecna.taglib.processor.JspAnnotationsProcessor;
import com.vecna.taglib.processor.JspModelMarshaller;
import com.vecna.taglib.processor.JspModelMarshaller.JspMarshallerException;
import com.vecna.taglib.processor.JspTagFileProcessor;

/**
 * JSP taglib generator Ant task
 * @author ogolberg@vecna.com
 */
public class JspTaglibGenerator extends Task {
  private String m_jspRoot;
  private String m_tagFileDir = "/WEB-INF/tags";
  private String m_packages;
  private String m_taglibDir;

  private String m_shortName;
  private String m_version;
  private String m_uri;

  private boolean m_lookInsideJars;

  private JspAnnotationsProcessor m_annotationsProcessor = new JspAnnotationsProcessor();
  private JspTagFileProcessor m_tagFileProcessor = new JspTagFileProcessor();
  private JspModelMarshaller m_marshaller = new JspModelMarshaller();

  /**
   * Set the root.
   * @param root The root to set
   */
  public void setJspRoot(String root) {
    m_jspRoot = root;
  }

  /**
   * Set the tagFileDir.
   * @param tagFileDir The tagFileDir to set
   */
  public void setTagFileDir(String tagFileDir) {
    m_tagFileDir = tagFileDir;
  }

  /**
   * Set the packages.
   * @param packages The packages to set
   */
  public void setPackages(String packages) {
    m_packages = packages;
  }

  /**
   * Set the taglibDir.
   * @param taglibDir The taglibDir to set
   */
  public void setTaglibDir(String taglibDir) {
    m_taglibDir = taglibDir;
  }

  /**
   * Set the version.
   * @param version The version to set
   */
  public void setVersion(String version) {
    m_version = version;
  }

  /**
   * Set the uri.
   * @param uri The uri to set
   */
  public void setUri(String uri) {
    m_uri = uri;
  }

  /**
   * Set the shortName.
   * @param shortName The shortName to set
   */
  public void setShortName(String shortName) {
    m_shortName = shortName;
  }

  /**
   * Whether to look inside JARs when scanning for tag classes
   * @param lookInsideJars whether to look insider JARs
   */
  public void setLookInsideJars(boolean lookInsideJars) {
    m_lookInsideJars = lookInsideJars;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() throws BuildException {
    JspTaglibModel taglib = new JspTaglibModel();
    taglib.shortName = m_shortName;
    taglib.version = m_version;
    taglib.uri = m_uri;

    m_annotationsProcessor.addLocalMetadata(StringUtils.split(m_packages, ','), taglib,
                                            getClass().getClassLoader(), m_lookInsideJars);
    if (m_jspRoot != null) {
      m_tagFileProcessor.addLocalMetadata(m_jspRoot, m_tagFileDir, taglib);
    }

    try {
      m_marshaller.marshal(taglib, new File(m_taglibDir + File.separator + m_shortName + ".tld"), getClass().getClassLoader());
    } catch (JspMarshallerException e) {
      throw new BuildException(e);
    }
  }
}