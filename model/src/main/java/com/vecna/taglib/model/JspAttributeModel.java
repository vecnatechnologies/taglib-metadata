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


/**
 * This class models JSP tag attributes
 * @author ogolberg@vecna.com
 */
public class JspAttributeModel {
  /**
   * Attribute name
   */
  public String name;
  /**
   * Whether the attribute is required
   */
  public boolean required;
  /**
   * Whether the attribute is a runtime expression
   */
  @XmlElement(name = "rtexprvalue") public boolean rtExprValue;
  /**
   * Attribute type
   */
  public String type;
  /**
   * Whether the attribute is a fragment
   */
  public boolean fragment;
  /**
   * Whether the attribute is a deferred value (JSP 2.1 only)
   */  
  @XmlElement(name = "deferred-value") public Boolean deferredValue;
  /**
   * Whether the attribute is a deferred method (JSP 2.1 only)
   */  
  @XmlElement(name = "deferred-method") public Boolean deferredMethod;
}
