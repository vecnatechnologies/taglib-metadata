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

package com.vecna.taglib.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Models JSP taglib
 * @author ogolberg@vecna.com
 */
@XmlRootElement(name = "taglib")
public class JspTaglibModel {
  /**
   * JSP version 2.0 descriptor
   */
  public static final String VERSION_20 = "2.0";
  /**
   * JSP version 2.1 descriptor
   */  
  public static final String VERSION_21 = "2.1";
  /**
   * Short name
   */
  @XmlElement(name = "short-name") public String shortName;
  /**
   * Taglib version
   */
  @XmlElement(name = "tlib-version") public String version;
  /**
   * Taglib uri
   */
  public String uri;
  /**
   * JSP version
   */
  @XmlAttribute(name = "version") public String jspVersion = VERSION_20;
  /**
   * Taglib info
   */
  public String info;
  /**
   * JSP tag classes
   */
  @XmlElement(name = "tag") public List<JspTagModel> tags = new ArrayList<JspTagModel>();
  /**
   * JSP tag files
   */
  @XmlElement(name = "tag-file") public List<JspTagFileModel> tagFiles = new ArrayList<JspTagFileModel>();
  /**
   * JSP functions
   */
  @XmlElement(name = "function") public List<JspFunctionModel> functions = new ArrayList<JspFunctionModel>();
}
