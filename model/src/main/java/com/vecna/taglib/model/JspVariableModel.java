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

import javax.xml.bind.annotation.XmlElement;

import com.vecna.taglib.annotations.JspVariableScope;

/**
 * Models JSP tag variables
 * @author ogolberg@vecna.com
 */
public class JspVariableModel {
  /**
   * Given name
   */  
  @XmlElement(name = "name-given") public String nameGiven;
  /**
   * Name inferred from an attribute
   */  
  @XmlElement(name = "name-from-attribute") public String nameFromAttribute;
  /**
   * Variable type
   */
  @XmlElement(name = "variable-class") public String variableClass;
  /**
   * Whether to declare the variable
   */
  public boolean declare;
  /**
   * Variable scope
   */
  public JspVariableScope scope;
}