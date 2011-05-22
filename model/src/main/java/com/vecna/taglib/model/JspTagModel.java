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

import javax.xml.bind.annotation.XmlElement;

import com.vecna.taglib.annotations.JspTagBodyContent;

/**
 * Models JSP tag classes
 * @author ogolberg@vecna.com
 */
public class JspTagModel {
  /**
   * Tag name
   */
  public String name;
  /**
   * Tag class
   */
  @XmlElement(name = "tag-class") public String tagClass;
  /**
   * Body content type
   */
  @XmlElement(name = "body-content") public JspTagBodyContent bodyContent;
  /**
   * Tag attributes
   */
  @XmlElement(name = "attribute") public List<JspAttributeModel> attributes = new ArrayList<JspAttributeModel>();
  /**
   * Tag variables
   */
  @XmlElement(name = "variable") public List<JspVariableModel> variables = new ArrayList<JspVariableModel>();
  /**
   * Whether the tag handles dynamic attributes
   */
  @XmlElement(name = "dynamic-attributes") public boolean dynamicAttributes;  
}