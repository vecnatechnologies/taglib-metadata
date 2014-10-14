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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.vecna.maven.commons.BuildClassPathMojo;
import com.vecna.taglib.model.JspTaglibModel;
import com.vecna.taglib.processor.JspAnnotationsProcessor;
import com.vecna.taglib.processor.JspModelMarshaller;
import com.vecna.taglib.processor.JspModelMarshaller.JspMarshallerException;
import com.vecna.taglib.processor.JspTagFileProcessor;

/**
 * Generates taglib from annotations.
 */
@Mojo(name = "taglib",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES,
      requiresDependencyResolution = ResolutionScope.COMPILE)
public class JspTaglibMetadataMojo extends BuildClassPathMojo {
  private final JspAnnotationsProcessor m_annotationsProcessor = new JspAnnotationsProcessor();
  private final JspTagFileProcessor m_tagFileProcessor = new JspTagFileProcessor();
  private final JspModelMarshaller m_marshaller = new JspModelMarshaller();

  /**
   * Maven project reference.
   */
  @Component
  private MavenProject project;

  /**
   * TLD output directory.
   *
   * @parameter expression="${project.build.directory}/generated-web-resources/WEB-INF"
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-web-resources/WEB-INF")
  private String taglibDir;

  /**
   * Webapp source directory.
   */
  @Parameter(defaultValue = "src/main/webapp")
  private String jspRoot;

  /**
   * JSP tags directory.
   */
  @Parameter(defaultValue = "/WEB-INF/tags")
  private String tagFileDir;

  /**
   * TLD short name.
   */
  @Parameter
  private String shortName;

  /**
   * TLD url.
   */
  @Parameter
  private String uri;

  /**
   * TLD version.
   */
  @Parameter
  private String version;

  /**
   * Packages with tag classes.
   */
  @Parameter
  private String[] packages;

  /**
   * Whether to introspect dependent jars for tag classes.
   */
  @Parameter
  private boolean lookInsideJars;

  /**
   * Skip.
   */
  @Parameter
  private boolean disabled = false;

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
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!disabled) {
      super.execute();
    } else {
      getLog().info("skipping taglib generation");
    }
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

    m_annotationsProcessor.addLocalMetadata(packages, taglib, lookInsideJars);
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