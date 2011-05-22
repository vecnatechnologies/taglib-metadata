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

package com.vecna.taglib.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.vecna.maven.commons.BuildClassPathMojo;
import com.vecna.taglib.model.JspTaglibModel;
import com.vecna.taglib.processor.JspAnnotationsProcessor;
import com.vecna.taglib.processor.JspModelMarshaller;
import com.vecna.taglib.processor.JspModelMarshaller.JspMarshallerException;
import com.vecna.taglib.processor.JspTagFileProcessor;

/**
 * Generates taglib from annotations
 *
 * @requiresDependencyResolution
 * @goal taglib
 * @phase process-classes
 */
public class JspTaglibMetadataMojo extends BuildClassPathMojo {
  private final JspAnnotationsProcessor m_annotationsProcessor = new JspAnnotationsProcessor();
  private final JspTagFileProcessor m_tagFileProcessor = new JspTagFileProcessor();
  private final JspModelMarshaller m_marshaller = new JspModelMarshaller();

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;


  /**
   * @parameter expression="${project.build.directory}/generated-web-resources/WEB-INF"
   */
  private String taglibDir;

  /**
   * @parameter default-value="src/main/webapp"
   */
  private String jspRoot;

  /**
   * @parameter default-value="/WEB-INF/tags"
   */
  private String tagFileDir;

  /**
   * @parameter
   */
  private String shortName;

  /**
   * @parameter
   */
  private String uri;

  /**
   * @parameter
   */
  private String version;

  /**
   * @parameter
   */
  private String[] packages;

  /**
   * {@inheritDoc}
   */
  @Override
  protected MavenProject getProject() {
    return project;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void executeWithClassLoader() throws MojoExecutionException {
    JspTaglibModel taglib = new JspTaglibModel();
    taglib.shortName = shortName;
    taglib.version = version;
    taglib.uri = uri;

    m_annotationsProcessor.addLocalMetadata(packages, taglib);
    if (jspRoot != null) {
      m_tagFileProcessor.addLocalMetadata(jspRoot, tagFileDir, taglib);
    }

    File dir = new File(taglibDir);
    if (!dir.exists()) {
      try {
        FileUtils.forceMkdir(dir);
      } catch (IOException e) {
        throw new MojoExecutionException("couldn't create directory " + dir);
      }
    }

    String taglibFile = taglibDir + File.separator + shortName + ".tld";
    getLog().info("generating " + taglibFile);
    try {
      m_marshaller.marshal(taglib, new File(taglibFile));
    } catch (JspMarshallerException e) {
      getLog().error(e);
      throw new MojoExecutionException("couldn't marshal taglib object");
    }
  }
}